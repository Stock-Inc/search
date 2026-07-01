export type FileUploadStatus = "failed" | "uploading" | "indexing" | "done";

export interface FileHandle {
    file: File,
    uploadStatus: FileUploadStatus
}