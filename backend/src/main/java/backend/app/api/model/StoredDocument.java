package backend.app.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class StoredDocument {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private int chunkCount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant uploadedAt;

    protected StoredDocument() {
    }

    public StoredDocument(UUID id, String fileName, long size, int chunkCount, String status, Instant uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.size = size;
        this.chunkCount = chunkCount;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
