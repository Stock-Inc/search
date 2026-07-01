package backend.app.api.repository;

import backend.app.api.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findTop50ByOrderByCreatedAtDesc();
}
