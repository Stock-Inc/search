import { UploadIcon } from "lucide-react";
import { Card, CardContent } from "../ui/card";
import { useRef, useState } from "react";
import UploadProgressDrawer from "../UploadProgressDrawer";
import { Field } from "../ui/field";
import { Input } from "../ui/input";
import { Button } from "../ui/button";

export default function Upload() {
    const [isDragging, setIsDragging] = useState(false);
    const dragCount = useRef(0);
    const [files, setFiles] = useState<Map<string, File>>(new Map());
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
    
    function handleDrop(e: React.DragEvent<HTMLDivElement>) {
        e.preventDefault();
        setIsDragging(false);
        const transferredFiles = e.dataTransfer.files;
        setFiles(s => {
            const newState = new Map(s);
            for (let i = 0; i < transferredFiles.length; i++) {
                const file = transferredFiles.item(i);    
                newState.set(file.name, file);
            }
            return newState;
        });
    }
    
    function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
        const fileList = e.target.files;
        setFiles(s => {
            const newState = new Map(s);
            for (let i = 0; i < fileList.length; i++) {
                const file = fileList.item(i);    
                newState.set(file.name, file);
            }
            return newState;
        });
    }
    
    return (
        <div className="w-full h-screen flex flex-col justify-between items-center p-6">
            <Field className="sm:w-xl" orientation="horizontal">
                <Input className="md:text-lg" placeholder="Search across uploaded files"/>
                <Button variant="secondary">
                    Search
                </Button>
            </Field>
            <Card
                className="border-2"
                onDragEnter={handleDragEnter}
                onDragLeave={handleDragLeave}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
            >
                <CardContent
                    className="max-sm:px-2 flex flex-col items-center sm:space-y-8 space-y-4 text-center"
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
            <div className="flex flex-col justify-center items-center space-y-2">
                <p className="flex flex-col text-center">
                    Uploading {files.size} files
                </p>
                <UploadProgressDrawer fileList={files}/>
            </div>
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