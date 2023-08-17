const CustomButton = ({ onClick, styles, btnText }) => {
    return (
        <button className={`text-xl text-white px-8 transition-all
                hover:bg-red-500 hover:bg-opacity-50 hover:text-white rounded-lg ${styles}`}
            onClick={onClick}>
            {btnText}
        </button>
    );
}

export default CustomButton;