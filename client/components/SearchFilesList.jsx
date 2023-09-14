import FileLine from "./FileLine";

const SearchFilesList = ({ filesList }) => {
    return (
        <div className="flex flex-col items-center">
            {filesList.map(file => (
                <FileLine file={file} />
            ))}
        </div>
    );
}
 
export default SearchFilesList;