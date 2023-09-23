import axios from "axios";
import { useRouter } from "next/router";
import { useEffect, useState } from "react";
import Head from "next/head";
import Navbar from "@/components/Navbar";
import LeftBar from "@/components/LeftBar";
import RightBar from "@/components/RightBar";
import SearchFilesList from "@/components/SearchFilesList";

const Search = () => {
  const router = useRouter();
  const [serverError, setServerError] = useState("");
  const [selectedFile, setSelectedFile] = useState(null);

  const [searchText, setSearchText] = useState("");
  const [filesList, setFilesList] = useState([]);

  useEffect(() => {
    axios({
      method: "GET",
      url: `${process.env.API_HOSTNAME}/auth/authenticate`,
      validateStatus: () => true,
      headers: {
        Authorization: "Bearer " + localStorage.getItem("token"),
      },
    }).then((resp) => {
      if (resp.status != 200) {
        router.push("/login");
        return null;
      }
    });
    // Make a search request here
  }, []);

  return (
    <>
      <Head>
        <title>FileMasterManager | Search</title>
      </Head>
      <div>
        <Navbar />
        <div className="flex h-screen text-white">
          <LeftBar />
          <div className="w-full h-full bg-gray-700">
            <div className="flex mt-6 mx-6">
              <input
                type="text"
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                placeholder="Search for files..."
                className="bg-gray-800 border-2 rounded-lg border-gray-600 p-2 transition outline-none focus:border-orange-500 focus:border-2 grow"
              />
              <button className="bg-gray-800 mt-2 pt-2 pb-2 rounded-md transition hover:bg-gray-600 ml-4 p-2">
                Search
              </button>
            </div>
            {serverError && (
              <p className="text-red-500 text-xl">{serverError}</p>
            )}
            <SearchFilesList filesList={filesList} />
          </div>
          <RightBar />
        </div>
      </div>
    </>
  );
};

export default Search;
