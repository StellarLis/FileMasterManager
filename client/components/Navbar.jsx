import Link from "next/link";

const Navbar = () => {
    return (
        <div className="bg-gradient-to-r from-gray-800 to-blue-950 text-white py-5 flex">
            <Link href='/'>
                <h1 className="text-2xl ml-5 font-bold">FileMasterManager</h1>
            </Link>
            <button className="ml-auto mr-6 text-xl text-red-500 px-8 transition-all
                hover:bg-red-500 hover:bg-opacity-50 hover:text-white rounded-lg">
                Quit
            </button>
        </div>
    );
}

export default Navbar;