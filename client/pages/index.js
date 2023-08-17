import LeftBar from "@/components/LeftBar";
import Navbar from "@/components/Navbar";
import RightBar from "@/components/RightBar";
import Head from "next/head";
import { useRouter } from "next/router";
import axios from 'axios';
import { useEffect, useState } from "react";
import FilesList from "@/components/FilesList";

const Home = () => {

    const router = useRouter();

    const [isLoading, setIsLoading] = useState(false);
    const [serverError, setServerError] = useState("");

    const [filesArray, setFilesArray] = useState([]);

    useEffect(() => {
        axios({
            method: "GET",
            url: "http://localhost:8080/auth/authenticate",
            validateStatus: () => true,
            headers: {
                'Authorization': "Bearer " + localStorage.getItem("token")
            }
        })
            .then(resp => {
                if (resp.status != 200) {
                    router.push("/login");
                    return null;
                }
                setIsLoading(true);
                axios({
                    method: "GET",
                    url: "http://localhost:8080/files/getFiles",
                    validateStatus: () => true,
                    headers: {
                        'Authorization': "Bearer " + localStorage.getItem("token")
                    }
                }).then(resp => {
                    setFilesArray(resp.data.files);
                    setIsLoading(false);
                    console.log(resp.data);
                    console.log(filesArray);
                })
            }).catch(err => {
                setServerError(err);
            });
    }, []);

    return (
        <>
            <Head>
                <title>FileMasterManager | Home</title>
            </Head>
            <div>
                <Navbar />
                <div className="flex h-screen">
                    <LeftBar />
                    <div className="w-full h-full bg-gray-700">
                        {isLoading && (<p className="text-white">Loading...</p>)}
                        {serverError && (<p className="text-red-500">{serverError}</p>)}
                        <FilesList filesList={filesArray} />
                    </div>
                    <RightBar />
                </div>
            </div>
        </>
    );
}

export default Home;