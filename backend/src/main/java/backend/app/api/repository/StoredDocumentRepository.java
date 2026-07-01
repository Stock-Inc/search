package backend.app.api.repository;

import backend.app.api.model.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, UUID> {
}
