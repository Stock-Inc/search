import { Link, useSearchParams } from "react-router";
import { Field } from "../ui/field";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { useState } from "react";
import { Home, SearchIcon } from "lucide-react";
import { useAtomValue } from "jotai";
import { fileAtom } from "@/lib/atoms";

export default function Search() {
    const files = useAtomValue(fileAtom);
    const [params, setParams] = useSearchParams();
    const [searchQuery, setSearchQuery] = useState(params.get("query") ?? "");
    function updateSearchQuery() {
        const nextParams = new URLSearchParams(params);
        nextParams.set("query", searchQuery);
        setParams(nextParams);
    }
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
                <Button aria-label="Search" disabled={files.size === 0} onClick={updateSearchQuery} variant="secondary">
                    <SearchIcon/>
                </Button>
                <Link to={"/"}>
                    <Button aria-label="Home" variant="secondary">
                        <Home/>
                    </Button>
                </Link>
            </Field>
            {
                files.size === 0 ? <p>Upload files to start searching!</p> : <p>Nothing found</p>
            }
        </div>
    );
}