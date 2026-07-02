import { Link, useSearchParams } from "react-router";
import { Field } from "../ui/field";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { useEffect, useState } from "react";
import { Home, SearchIcon } from "lucide-react";
import { useAtomValue } from "jotai";
import { fileAtom } from "@/lib/atoms";
import { Tooltip, TooltipContent, TooltipTrigger } from "../ui/tooltip";
import type { FileSearchResult } from "@/lib/types";
import ResultCard from "../ResultCard";
import { ScrollArea } from "../ui/scroll-area";

export default function Search() {
    const files = useAtomValue(fileAtom);
    const [params, setParams] = useSearchParams();
    const [searchQuery, setSearchQuery] = useState(params.get("query") ?? "");
    const [searchResults, setSearchResults] = useState<FileSearchResult[]>([]);
    function updateSearchQuery() {
        const nextParams = new URLSearchParams(params);
        nextParams.set("query", searchQuery);
        setParams(nextParams);
    }
    
    useEffect(() => {
        if (params.get("query").trim().length === 0) return;
        fetch(`${import.meta.env.VITE_BACKEND_URL}/search?q=${params.get("query")}&page=0&size=10`)
            .then(r => {
                if (r.ok) {
                    r.json().then(r => {
                        console.log(r);
                        setSearchResults(r);
                    })
                } else {
                    r.json().then(r => console.log(r))
                }
            })
            .catch(e => {
                console.log(e);
            });
    }, [params]);
    
    return (
        <div className="flex flex-col h-screen w-full space-y-8 sm:p-10 p-4 items-center">
            <Field className="px-4 pt-4 max-w-xl" orientation="horizontal">
                <Input
                    className="md:text-lg"
                    placeholder="Search across uploaded files"
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    onKeyDown={e => {
                        if (e.key === "Enter") {
                            if (files.size === 0) return;
                            updateSearchQuery();
                        }
                    }}
                />
                <Tooltip>
                    <TooltipTrigger render={
                        <Button aria-label="Search" disabled={files.size === 0} onClick={updateSearchQuery} variant="secondary">
                            <SearchIcon/>
                        </Button> 
                    }/>
                    <TooltipContent>
                        Search
                    </TooltipContent>
                </Tooltip>
                <Tooltip>
                    <TooltipTrigger render={
                        <Link to={"/"}>
                            <Button aria-label="Home" variant="secondary">
                                <Home/>
                            </Button>
                        </Link>
                    }/>
                    <TooltipContent>
                        Home
                    </TooltipContent>
                </Tooltip>
            </Field>
            <ScrollArea className="overflow-auto">
                
                {
                    (function () {
                        if (files.size === 0) {
                            return <p>Upload files to start searching!</p>
                        } else if (searchResults.length === 0) {
                            return <p>Nothing found</p>
                        } else {
                            return (
                                searchResults.map(r => <ResultCard key={r.chunk_id} result={r}/>)
                            );
                        }
                    })()
                }
            </ScrollArea>
        </div>
    );
}