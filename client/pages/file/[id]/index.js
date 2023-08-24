import Navbar from "@/components/Navbar";
import CustomButton from "@/components/UI/CustomButton";
import axios from "axios";
import Image from "next/image";
import { useRouter } from "next/router";
import { useEffect, useState } from "react";

const FilePage = () => {
    const router = useRouter();
    const { id } = router.query;

    const [isLoading, setIsLoading] = useState(false);
    const [serverError, setServerError] = useState("");
    const [file, setFile] = useState();

    useEffect(() => {
        axios({
            method: "GET",
            url: `http://localhost:8080/files/getFileById/${id}`,
            validateStatus: () => true,
            headers: {
                'Authorization': "Bearer " + localStorage.getItem("token")
            }
        }).then(resp => {
            setIsLoading(true);
            if (resp.status == 401) {
                router.push("/login");
                return null;
            }
            if (resp.status == 404) {
                setServerError("404 File not found");
                return null;
            }
            setFile(resp.data);
            setIsLoading(false);
        }).catch(err => {
            setServerError(err);
        });
    }, []);

    const onDownload = () => {
        const myHeaders = new Headers();
        myHeaders.append('Authorization', "Bearer " + localStorage.getItem("token"));
        fetch(`http://localhost:8080/files/download/${id}`, {
            method: "GET",
            headers: myHeaders
        }).then(response => response.blob())
            .then(blob => {
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = file.filename;
                a.click();
                URL.revokeObjectURL(url);
            }).catch(err => {
                console.log(err);
            })
    };

    const onDelete = () => {
        axios({
            method: "DELETE",
            url: `http://localhost:8080/files/deleteFileById/${id}`,
            validateStatus: () => true,
            headers: {
                'Authorization': "Bearer " + localStorage.getItem("token")
            }
        }).then(resp => {
            if (resp.status != 200) {
                setServerError("Failed to delete");
                return null;
            }
            router.push("/");
        }).catch(err => {
            console.log(err);
        })
    };

    return (
        <div>
            <Navbar />
            <div className="bg-gray-700 h-screen flex flex-col items-center text-white">
                {serverError && <p className="text-red-500 font-bold">{serverError}</p>}
                {isLoading && <p className="font-bold">Loading...</p>}
                {file && (
                    <>
                        <Image src="/white-file-icon.png" height={200} width={150} alt="white-file" />
                        <p className="font-bold text-3xl">{file.filename}</p>
                        <p>File owner: {file.owner}</p>
                        <CustomButton onClick={onDownload} btnText="Download"
                            styles="bg-green-500 mt-4 hover:bg-green-800" />
                        <CustomButton onClick={onDelete} btnText="Delete"
                            styles="bg-red-500 mt-4 hover:bg-red-700" />
                    </>
                )}
            </div>
        </div>
    );
}

export default FilePage;