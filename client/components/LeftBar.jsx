import CustomButton from "./UI/CustomButton";

const LeftBar = () => {
    return (
        <div className="bg-gray-900 w-64 flex flex-col items-center">
            <CustomButton styles={"text-white hover:bg-white py-2 text-white mt-2"}
                btnText={"My Files"} />
        </div>
    );
}

export default LeftBar;