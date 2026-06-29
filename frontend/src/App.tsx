import { createBrowserRouter, RouterProvider } from "react-router";
import Upload from "./components/pages/Upload";

function App() {

    const router = createBrowserRouter([
        {
            path: "/",
            Component: Upload
        }
    ]);
    
    return (
        <RouterProvider router={router}/>
    );
}

export default App
