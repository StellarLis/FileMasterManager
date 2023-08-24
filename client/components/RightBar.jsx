import { useRouter } from "next/router";
import CustomButton from "./UI/CustomButton";

const RightBar = () => {
    const router = useRouter();

    const onClick = () => {
        router.push("/file/upload");
    };

    return (
        <div className="bg-gray-900 ml-auto w-64 flex flex-col items-center">
            <CustomButton btnText="Upload" styles="text-white hover:bg-white py-2 text-white mt-2"
                onClick={onClick} />
        </div>
    );
}

export default RightBar;