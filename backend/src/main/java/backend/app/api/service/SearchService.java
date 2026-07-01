package backend.app.api.service;

import backend.app.api.dto.SearchResult;
import backend.app.api.DocumentChunk;
import backend.app.api.dto.SearchHistoryResponse;
import backend.app.api.model.SearchHistory;
import backend.app.api.repository.SearchHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final String INDEX_MAPPING = """
            {
              "settings": {
                "analysis": {
                  "filter": {
                    "russian_stop": { "type": "stop", "stopwords": "_russian_" },
                    "russian_stemmer": { "type": "stemmer", "language": "russian" }
                  },
                  "analyzer": {
                    "analysis-ru": {
                      "tokenizer": "standard",
                      "filter": ["lowercase", "russian_stop", "russian_stemmer"]
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "chunk_id": { "type": "keyword" },
                  "file_name": { "type": "keyword" },
                  "page_number": { "type": "integer" },
                  "text": { "type": "text", "analyzer": "analysis-ru" }
                }
              }
            }
            """;

    private final RestClient elastic;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final SearchHistoryRepository searchHistoryRepository;

    public SearchService(
            StringRedisTemplate redis,
            SearchHistoryRepository searchHistoryRepository,
            @Value("${elasticsearch.url}") String elasticsearchUrl
    ) {
        this.elastic = RestClient.builder().baseUrl(elasticsearchUrl).build();
        this.redis = redis;
        this.objectMapper = new ObjectMapper();
        this.searchHistoryRepository = searchHistoryRepository;
    }

    /**
     * Creates the Elasticsearch index with Russian analyzer when the app is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createIndex() throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            try {
                if (indexExists()) {
                    log.info("Elasticsearch index documents is ready");
                    return;
                }

                elastic.put()
                        .uri("/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(INDEX_MAPPING)
                        .exchange((request, response) -> {
                            HttpStatusCode statusCode = response.getStatusCode();
                            if (statusCode.is2xxSuccessful() || statusCode.value() == 400) {
                                return null;
                            }
                            throw new IllegalStateException("Unexpected index create status: " + statusCode);
                        });
                log.info("Elasticsearch index documents is ready");
                return;
            } catch (Exception e) {
                log.warn("Elasticsearch is unavailable, retry {}/30", i + 1);
                Thread.sleep(2000);
            }
        }
        throw new IllegalStateException("Elasticsearch is unavailable");
    }

    /**
     * Indexes all chunks of one document into Elasticsearch.
     *
     * @param documentId unique document id
     * @param fileName source file name
     * @param chunks extracted text chunks
     */
    public void indexChunks(UUID documentId, String fileName, List<DocumentChunk> chunks) {
        log.info("Indexing started documentId={} fileName={} chunks={}", documentId, fileName, chunks.size());
        for (DocumentChunk chunk : chunks) {
            String chunkId = documentId + "-" + chunk.chunkIndex();
            Map<String, Object> body = Map.of(
                    "chunk_id", chunkId,
                    "file_name", fileName,
                    "page_number", chunk.pageNumber(),
                    "text", chunk.text()
            );

            elastic.put()
                    .uri("/documents/_doc/{id}", chunkId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        }
        log.info("Indexing completed documentId={} fileName={} chunks={}", documentId, fileName, chunks.size());
    }

    /**
     * Executes full-text search with pagination and Redis caching.
     *
     * @param query search query text
     * @param page zero-based page
     * @param size page size
     * @return list of search hits
     */
    public List<SearchResult> search(String query, int page, int size) throws Exception {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be empty");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String normalizedQuery = query.trim();
        String cacheKey = "search:" + normalizedQuery + ":" + safePage + ":" + safeSize;
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null) {
                List<SearchResult> results = objectMapper.readValue(cached, new TypeReference<>() {
                });
                saveHistory(normalizedQuery, results.size());
                log.info("Search cache hit query=\"{}\" results={}", normalizedQuery, results.size());
                return results;
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis is unavailable, proceeding without cache");
        }

        Map<String, Object> request = Map.of(
                "from", safePage * safeSize,
                "size", safeSize,
                "_source", List.of("chunk_id", "file_name", "page_number", "text"),
                "query", Map.of(
                        "multi_match", Map.of(
                                "query", normalizedQuery,
                                "fields", List.of("text")
                        )
                ),
                "highlight", Map.of(
                        "pre_tags", List.of("<mark>"),
                        "post_tags", List.of("</mark>"),
                        "fields", Map.of(
                                "text", Map.of(
                                        "number_of_fragments", 1,
                                        "fragment_size", 220
                                )
                        )
                )
        );

        Map<String, Object> response = elastic.post()
                .uri("/documents/_search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });

        List<SearchResult> results = toResults(response);
        try {
            redis.opsForValue().set(cacheKey, objectMapper.writeValueAsString(results), Duration.ofMinutes(5));
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis is unavailable, skipping cache write");
        }
        saveHistory(normalizedQuery, results.size());
        log.info("Search executed query=\"{}\" page={} size={} results={}", normalizedQuery, safePage, safeSize, results.size());
        return results;
    }

    /**
     * Returns last 50 search queries.
     */
    public List<SearchHistoryResponse> history() {
        return searchHistoryRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(item -> new SearchHistoryResponse(
                        item.getId(),
                        item.getQuery(),
                        item.getResultCount(),
                        item.getCreatedAt()
                ))
                .toList();
    }

    private void saveHistory(String query, int resultCount) {
        searchHistoryRepository.save(new SearchHistory(query, resultCount));
    }

    @SuppressWarnings("unchecked")
    private List<SearchResult> toResults(Map<String, Object> response) {
        if (response == null) {
            return Collections.emptyList();
        }

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) hits.get("hits");
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<SearchResult> results = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Map<String, Object> source = (Map<String, Object>) item.get("_source");
            if (source == null) {
                continue;
            }
            String snippet = extractSnippet(item, source);
            Number score = (Number) item.getOrDefault("_score", 0d);
            Number pageNumber = (Number) source.getOrDefault("page_number", 1);
            results.add(new SearchResult(
                    String.valueOf(source.getOrDefault("chunk_id", "")),
                    String.valueOf(source.getOrDefault("file_name", "")),
                    pageNumber.intValue(),
                    snippet,
                    score.doubleValue()
            ));
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private String extractSnippet(Map<String, Object> item, Map<String, Object> source) {
        Object highlightObj = item.get("highlight");
        if (highlightObj instanceof Map<?, ?> highlightMapAny) {
            Map<String, Object> highlightMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : highlightMapAny.entrySet()) {
                if (entry.getKey() != null) {
                    highlightMap.put(entry.getKey().toString(), entry.getValue());
                }
            }
            Object textHighlights = highlightMap.get("text");
            if (textHighlights instanceof List<?> values && !values.isEmpty() && values.getFirst() != null) {
                return values.getFirst().toString();
            }
        }
        String fullText = String.valueOf(source.getOrDefault("text", ""));
        if (fullText.length() <= 240) {
            return fullText;
        }
        return fullText.substring(0, 240) + "...";
    }

    private boolean indexExists() {
        return elastic.head()
                .uri("/documents")
                .exchange((request, response) -> response.getStatusCode().is2xxSuccessful());
    }
}
