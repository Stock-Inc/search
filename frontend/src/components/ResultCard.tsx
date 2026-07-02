import type { FileSearchResult } from "@/lib/types";
import { Card, CardContent } from "./ui/card";

export default function ResultCard(
    {
        result
    }: {
        result: FileSearchResult
    }
) {
    const parts = result.text.split(/(<mark>.*?<\/mark>)/g);
    return (
        <Card className="my-4 py-4">
            <CardContent className="w-full text-lg h-full px-4 flex flex-col space-y-2">
                <div className="flex justify-between w-full">
                    <b>{ result.file_name }</b>
                    <p>Page { result.page }</p>
                </div>
                <p className="px-2">
                    {
                        parts.map((p, i) => {
                            const match = p.match(/^<mark>(.*?)<\/mark>$/);
                            if (match) {
                                return <mark key={result.chunk_id + i}>{ match[1] }</mark>
                            }
                            return p;
                        })
                    }
                </p> 
            </CardContent>
        </Card>
    );
}