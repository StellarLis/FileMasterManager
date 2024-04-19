import FileGrid from "./FileGrid";

const FilesList = ({ filesList }) => {
  if (filesList != undefined) {
    return (
      <div className="grid grid-cols-6 gap-4">
        {filesList.map((file) => (
          <FileGrid key={file.id} file={file} />
        ))}
      </div>
    );
  }
};

export default FilesList;
