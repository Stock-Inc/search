import { useEffect } from "react";
import { Drawer, DrawerContent, DrawerTrigger } from "./ui/drawer";
import { Card, CardContent } from "./ui/card";
import { Spinner } from "./ui/spinner";
import { Check, CloudAlert } from "lucide-react";
import { Button } from "./ui/button";
import { ScrollArea } from "./ui/scroll-area";

function Content(
    {
        fileList
    }: {
        fileList: Map<string, File>
    }
) {
    return (
        <DrawerContent className="h-full">
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
        </DrawerContent>
    );
}

export default function UploadProgressDrawer(
    {
        fileList
    }: {
        fileList: Map<string, File>
    }
) {
    useEffect(() => {
        console.log(fileList.size);
    }, [fileList])
    return (
        <>
            <div className="sm:hidden">
                <Drawer modal={false}>
                    <DrawerTrigger asChild>
                        <Button variant="outline">
                            View Upload Progress
                        </Button>
                    </DrawerTrigger>
                    <Content fileList={fileList}/>
                </Drawer>
            </div>
            <div className="max-sm:hidden">
                <Drawer direction="right" modal={false}>
                    <DrawerTrigger asChild>
                        <Button variant="outline">
                           View Upload Progress 
                        </Button>
                    </DrawerTrigger>
                    <Content fileList={fileList}/>
                </Drawer>
            </div> 
        </>
    );
}