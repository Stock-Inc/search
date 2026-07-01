import { Link, useSearchParams } from "react-router";
import { Field } from "../ui/field";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { useState } from "react";
import { Home, SearchIcon } from "lucide-react";

export default function Search() {
    const [params, setParams] = useSearchParams();
    const [searchQuery, setSearchQuery] = useState(params.get("query"));
    function updateSearchQuery() {
        const nextParams = new URLSearchParams(params);
        nextParams.set("query", searchQuery);
        setParams(nextParams);
    }
    return (
        <div className="flex flex-col h-screen w-full space-y-8 sm:p-10 p-4 items-center">
            <Field className="px-4 pt-4 max-w-xl" orientation="horizontal">
                <Input
                    className="md:text-lg "
                    placeholder="Search across uploaded files"
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                />
                <Button onClick={updateSearchQuery} variant="secondary">
                    <SearchIcon/>
                </Button>
                <Link to={"/"}>
                    <Button variant="secondary">
                        <Home/>
                    </Button>
                </Link>
            </Field>
            <p>Nothing found</p>
        </div>
    );
}