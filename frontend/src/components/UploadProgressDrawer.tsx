import { Card, CardContent } from "./ui/card";
import { Spinner } from "./ui/spinner";
import { Check, CloudAlert } from "lucide-react";
import { ScrollArea } from "./ui/scroll-area";
import { Sheet, SheetContent } from "./ui/sheet";
import { Field } from "./ui/field";
import { Input } from "./ui/input";
import { Button } from "./ui/button";
import { Separator } from "./ui/separator";
import type { FileHandle } from "@/lib/types";
import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { Tooltip, TooltipContent, TooltipTrigger } from "./ui/tooltip";

export default function UploadProgressDrawer(
    {
        fileList
    }: {
        fileList: Map<string, FileHandle>
    }
) {
    const [searchQuery, setSearchQuery] = useState("");
    const navigate = useNavigate();
    
    return (
            <Sheet open={fileList.size > 0} modal={false} disablePointerDismissal>
                <SheetContent showCloseButton={false} side="bottom" className={"max-h-[50vh] overflow-auto flex flex-col space-y-4"}> 
                    <Field className="px-4 pt-4" orientation="horizontal">
                        <Input
                            className="md:text-lg "
                            placeholder="Search across uploaded files"
                            value={searchQuery}
                            onChange={e => setSearchQuery(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === "Enter") {
                                    navigate(`search?query=${searchQuery}`);
                                }
                            }}
                        />
                            <Link to={`search?query=${searchQuery}`}>
                                <Button variant="secondary">
                                    Search
                                </Button>
                            </Link>
                    </Field>
                    <Separator/>
                    <ScrollArea className={"overflow-auto"}>
                        {
                            [...fileList].map(([name, handle]) => (
                                <Card className="" key={name}>
                                    <CardContent className="flex justify-between items-center">
                                        <p className="text-lg text-ellipsis max-w-[25ch] wrap-break-word line-clamp-1">
                                            {name}
                                        </p>
                                        { 
                                            (function () {
                                                switch (handle.uploadStatus) {
                                                    case "failed": return (
                                                        <Tooltip>
                                                            <TooltipTrigger>
                                                                <CloudAlert aria-label="Failed" className="text-red-500" size={25} />
                                                            </TooltipTrigger>
                                                            <TooltipContent>Failed</TooltipContent>
                                                        </Tooltip>
                                                    );
                                                    case "uploading": return (
                                                        <Tooltip>
                                                            <TooltipTrigger>
                                                                <Spinner aria-label="Uploading" className="scale-150" />
                                                            </TooltipTrigger>
                                                            <TooltipContent>
                                                                Upload in progress
                                                            </TooltipContent>
                                                        </Tooltip>
                                                    );
                                                    case "indexing": return (
                                                        <Spinner aria-label="Uploading" className="scale-150" />
                                                    );
                                                    case "done": return (
                                                        <Tooltip>
                                                            <TooltipTrigger>
                                                                <Check aria-label="Done" className="text-green-500" size={25} />
                                                            </TooltipTrigger>
                                                            <TooltipContent>
                                                                Upload complete
                                                            </TooltipContent>
                                                        </Tooltip>
                                                    );
                                                }
                                            })()
                                        }
                                    </CardContent>
                                </Card>
                            ))
                        }
                    </ScrollArea>
                </SheetContent>
            </Sheet>
    );
}