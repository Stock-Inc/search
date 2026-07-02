export type FileUploadStatus = "failed" | "uploading" | "indexing" | "done";

export interface FileHandle {
    fileName: string,
    uploadStatus: FileUploadStatus
}

export interface FileSearchResult {
    chunk_id: string,
    file_name: string,
    page: number,
    score: number,
    text: string
}

export interface FileServerEntry {
    documentId: string,
    fileName: string,
    size: number,
    chunkCount: number,
    status: string,
    uploadedAt: string
}