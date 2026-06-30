import { Card, CardContent } from "./ui/card";
import { Spinner } from "./ui/spinner";
import { Check, CloudAlert } from "lucide-react";
import { ScrollArea } from "./ui/scroll-area";
import { Sheet, SheetContent } from "./ui/sheet";
import { Field } from "./ui/field";
import { Input } from "./ui/input";
import { Button } from "./ui/button";
import { Separator } from "./ui/separator";

export default function UploadProgressDrawer(
    {
        fileList
    }: {
        fileList: Map<string, File>
    }
) {
    return (
            <Sheet open={fileList.size > 0} modal={false} disablePointerDismissal>
                <SheetContent showCloseButton={false} side="bottom" className={"max-h-[50vh] overflow-auto flex flex-col space-y-4"}> 
                    <Field className="px-4 pt-4" orientation="horizontal">
                        <Input className="md:text-lg " placeholder="Search across uploaded files"/>
                        <Button variant="secondary">
                            Search
                        </Button>
                    </Field>
                    <Separator/>
                    <ScrollArea className={"overflow-auto"}>
                        {
                            [...fileList].map(([name, file]) => (
                                <Card className="" key={name}>
                                    <CardContent className="flex justify-between items-center">
                                        <p className="text-lg text-ellipsis max-w-[25ch] wrap-break-word line-clamp-1">
                                            {name}
                                        </p>
                                        { 
                                            //TODO: replace with actual progress handling logic and add tooltips
                                            true ? <Spinner className="scale-150" />
                                                : true ? <Check className="text-green-500" size={25} />
                                                    : <CloudAlert className="text-red-500" size={25} />
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