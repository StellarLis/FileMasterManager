import Image from "next/image";
import { useRouter } from "next/router";

const FileLine = ({ file }) => {
  const router = useRouter();

  const onFileClick = () => {
    router.push(`/file/${file.id}`);
  };

  return (
    <div
      className="bg-gray-600 text-white border-2 border-black m-2 rounded-xl flex
        items-center break-all transition-all hover:bg-gray-500 cursor-pointer p-4 mx-4"
      onClick={() => onFileClick()}
    >
      <Image src="/white-file-icon.png" width={100} height={150} />
      <div className="flex flex-col">
        <p className="text-xl font-bold">{file.filename}</p>
        <p>Owner: {file.fileUser.username}</p>
      </div>
    </div>
  );
};

export default FileLine;
