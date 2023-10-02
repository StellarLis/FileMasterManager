import { useRouter } from "next/router";
import CustomButton from "./UI/CustomButton";

const LeftBar = () => {
    const router = useRouter();
    const onMyFiles = () => {
        router.push('/');
    }

    const onSearch = () => {
        router.push('/search');
    }

    return (
        <div className="bg-gray-900 w-64 flex flex-col items-center">
            <CustomButton styles={"text-white hover:bg-white py-2 text-white mt-2"}
                btnText={"My Files"} onClick={onMyFiles} />
            <CustomButton styles={"text-white hover:bg-white py-2 text-white mt-2"}
                btnText={"Search"} onClick={onSearch} />
        </div>
    );
}

export default LeftBar;