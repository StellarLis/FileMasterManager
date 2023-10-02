import FileLine from "./FileLine";

const SearchFilesList = ({ filesList }) => {
  return (
    <div className="flex flex-col">
      {filesList.map((file) => (
        <FileLine file={file} key={file.id} />
      ))}
    </div>
  );
};

export default SearchFilesList;
