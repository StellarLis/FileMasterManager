import Navbar from "@/components/Navbar";
import { useState, useEffect } from "react";
import axios from "axios";
import Image from "next/image";
import CustomButton from "@/components/UI/CustomButton";
import { useRouter } from "next/router";

const UploadPage = () => {
    const router = useRouter();
    const [serverError, setServerError] = useState("");
    const [selectedFile, setSelectedFile] = useState(null);

    useEffect(() => {
        axios({
            method: "GET",
            url: `${process.env.API_HOSTNAME}/auth/authenticate`,
            validateStatus: () => true,
            headers: {
                'Authorization': "Bearer " + localStorage.getItem("token")
            }
        }).then(resp => {
            if (resp.status != 200) {
                router.push("/login");
                return null;
            }
        })
    }, []);

    const onChange = e => {
        setSelectedFile(e.target.files[0]);
    }
    const fileData = () => {
        if (selectedFile) {
            return (<div className="flex flex-col items-center">
                <Image src="/white-file-icon.png" height={200} width={150} alt="white-file" />
                <p className="text-xl font-semibold">{selectedFile.name}</p>
            </div>)
        }
    }
    const onUpload = () => {
        const formData = new FormData();
        formData.append("multipartFile", selectedFile);
        axios.post(`${process.env.API_HOSTNAME}/files/upload`, formData, {
            headers: {
                'Authorization': "Bearer " + localStorage.getItem("token")
            }
        }).then(() => router.push('/'));
    }

    return (
        <div>
            <Navbar />
            <div className="bg-gray-700 h-screen flex flex-col items-center justify-center
                text-white">
                {fileData()}
                {serverError && <p className="text-red-500">{serverError}</p>}
                <input type="file" onChange={onChange} className="border mt-4" />
                <CustomButton onClick={onUpload} btnText="Upload"
                    styles="bg-green-500 mt-4 hover:bg-green-700" />
            </div>
        </div>
    );
}

export default UploadPage;