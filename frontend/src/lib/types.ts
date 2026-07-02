export type FileUploadStatus = "failed" | "uploading" | "indexing" | "done";

export interface FileHandle {
    file: File,
    uploadStatus: FileUploadStatus
}

export interface FileSearchResult {
    chunk_id: string,
    file_name: string,
    page: number,
    score: number,
    text: string
}