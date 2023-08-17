import Link from "next/link";
import CustomButton from "./UI/CustomButton";

const Navbar = () => {

    const onQuit = () => {

    }

    return (
        <div className="bg-gradient-to-r from-gray-800 to-blue-950 text-white py-5 flex">
            <Link href='/'>
                <h1 className="text-2xl ml-5 font-bold">FileMasterManager</h1>
            </Link>
            <CustomButton styles={"ml-auto mr-6 text-red-500"} btnText={"Quit"} />
        </div>
    );
}

export default Navbar;