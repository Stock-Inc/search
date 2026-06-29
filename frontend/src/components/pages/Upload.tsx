import { UploadIcon } from "lucide-react";
import { Card } from "../ui/card";
import { useRef, useState } from "react";

export default function Upload() {
    const [isDragging, setIsDragging] = useState(false);
    const dragCount = useRef(0);
    const [files, setFiles] = useState(new Map());
    
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
    
    function handleDrop(e: React.DragEvent<HTMLDivElement>) {
        e.preventDefault();
        setIsDragging(false);
        const transferredFiles = e.dataTransfer.files;
        setFiles(s => {
            for (let i = 0; i < transferredFiles.length; i++) {
                const file = transferredFiles.item(i);    
                s.set(file.name, file);
            }
            return s;
        });
    }
    
    return (
        <div className="w-full h-screen flex flex-col justify-center items-center">
            <Card
                className="p-10 border-2 flex flex-col items-center space-y-8"
                onDragEnter={handleDragEnter}
                onDragLeave={handleDragLeave}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
            >
                <p className="text-2xl">Upload a file for scanning</p>
                <UploadIcon className={`transition-all size-20 ${isDragging && "scale-125"}`} />
                <p className="text-xl underline">Drag and drop or click to select</p>
            </Card>
            <input
                type="file"
                hidden
                multiple
            />
        </div>
    )
}