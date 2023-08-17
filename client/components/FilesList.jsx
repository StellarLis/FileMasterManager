import FileGrid from "./FileGrid";

const FilesList = ({ filesList }) => {
    return (
        <div>
            {filesList.map(file => (
                <FileGrid file={file} />
            ))}
        </div>
    );
}

export default FilesList;