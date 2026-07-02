import { UploadIcon } from "lucide-react";
import { Card, CardContent } from "../ui/card";
import { useRef, useState } from "react";
import UploadProgressDrawer from "../UploadProgressDrawer";
import { useAtom } from "jotai";
import { fileAtom } from "@/lib/atoms";

export default function Upload() {
    const [isDragging, setIsDragging] = useState(false);
    const dragCount = useRef(0);
    const [files, setFiles] = useAtom(fileAtom);
    const inputRef = useRef(null);
    
    function handleDragEnter(e: React.DragEvent<HTMLDivElement>) {
        e.preventDefault();
        dragCount.current++;
        setIsDragging(true);
    }
    
    function handleDragLeave(e: React.DragEvent<HTMLDivElement>) {
        e.preventDefault();
        dragCount.current--;
        if (dragCount.current === 0) {
            setIsDragging(false);
        }
    }
    
    function handleDragOver(e: React.DragEvent<HTMLDivElement>) {
        e.preventDefault();
    }
    
    function uploadFiles(fileList: FileList) {
        setFiles(s => {
            const newState = new Map(s);
            for (let i = 0; i < fileList.length; i++) {
                const file = fileList.item(i);
                if (newState.has(file.name)) continue;
                newState.set(file.name, { file, uploadStatus: "uploading" });
                const formData = new FormData();
                formData.append("file", file);
                fetch(`${import.meta.env.VITE_BACKEND_URL}/documents/upload`, {
                    method: "POST",
                    body: formData
                }).then(r => {
                    if (r.ok) {
                        r.json().then(r => console.log(r));
                        setFiles(prev => {
                            const updated = new Map(prev);
                            updated.set(file.name, { file, uploadStatus: "done" });
                            return updated;
                        });
                    } else {
                        r.json().then(r => {
                            console.log(r)
                            setFiles(prev => {
                                const updated = new Map(prev);
                                updated.set(file.name, { file, uploadStatus: "failed" });
                                return updated;
                            });
                        });
                    }
                }).catch(e => {
                    console.log(e);
                    setFiles(prev => {
                        const updated = new Map(prev);
                        updated.set(file.name, { file, uploadStatus: "failed" });
                        return updated;
                    });
                });
            }
            return newState;
        });
    }
    
    function handleDrop(e: React.DragEvent<HTMLDivElement>) {
        e.preventDefault();
        setIsDragging(false);
        const transferredFiles = e.dataTransfer.files;
        uploadFiles(transferredFiles)
    }
    
    function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
        const fileList = e.target.files;
        uploadFiles(fileList);
    }
    
    return (
        <div className="w-full h-screen flex flex-col space-y-10 items-center p-6">
            <Card
                className="border-2 my-auto"
                onDragEnter={handleDragEnter}
                onDragLeave={handleDragLeave}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
            >
                <CardContent
                    className="flex flex-col items-center sm:space-y-8 space-y-4 text-center"
                >
                    <p className="text-2xl">Upload a file for scanning</p>
                    <UploadIcon className={`transition-all size-15 sm:size-20 ${isDragging && "scale-125"}`} />
                    <p
                        className="text-xl underline cursor-pointer"
                        onClick={_ => {
                            inputRef.current.click();
                        }}
                    >Drag and drop or click to select</p>
                </CardContent>
            </Card>
            <div aria-hidden className="h-[50vh]"/>
            <UploadProgressDrawer fileList={files}/>
            <input
                ref={inputRef}
                type="file"
                hidden
                multiple
                onChange={handleInputChange}
            />
        </div>
    )
}