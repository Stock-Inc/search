package backend.app.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String query;

    @Column(nullable = false)
    private int resultCount;

    @Column(nullable = false)
    private Instant createdAt;

    protected SearchHistory() {
    }

    public SearchHistory(String query, int resultCount) {
        this.query = query;
        this.resultCount = resultCount;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public int getResultCount() {
        return resultCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
