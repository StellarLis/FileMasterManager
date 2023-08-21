import Link from "next/link";
import CustomButton from "./UI/CustomButton";
import { useRouter } from "next/router";

const Navbar = () => {
    const router = useRouter();

    const onQuit = () => {
        localStorage.removeItem("token");
        router.push('/login');
    }

    return (
        <div className="bg-gradient-to-r from-gray-800 to-blue-950 text-white py-5 flex">
            <Link href='/'>
                <h1 className="text-2xl ml-5 font-bold">FileMasterManager</h1>
            </Link>
            <CustomButton styles={"ml-auto mr-6 text-red-500"} btnText={"Quit"} onClick={onQuit} />
        </div>
    );
}

export default Navbar;