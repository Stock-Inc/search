import { createBrowserRouter, RouterProvider } from "react-router";
import Upload from "./components/pages/Upload";
import Search from "./components/pages/Search";

function App() {

    const router = createBrowserRouter([
        {
            path: "/",
            Component: Upload
        },
        {
            path: "search",
            Component: Search
        }
    ]);
    
    return (
        <RouterProvider router={router}/>
    );
}

export default App
