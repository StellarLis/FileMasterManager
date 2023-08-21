import Image from "next/image";
import { useRouter } from "next/router";

const FileGrid = ({ file }) => {
    const router = useRouter();

    const onClick = () => {
        router.push(`/file/${file.id}`);
    }

    return (
        <div className="bg-gray-600 text-white border-2 border-black m-2 rounded-xl flex flex-col
            items-center break-all transition-all hover:bg-gray-500 cursor-pointer"
            onClick={onClick}>
            <Image src='/white-file-icon.png' width={100} height={150} />
            <p>{file.filename}</p>
        </div>
    );
}

export default FileGrid;