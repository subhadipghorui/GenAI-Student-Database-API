import React, { useState, useMemo } from "react";
import { TextField, Box } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import {MaterialReactTable} from "material-react-table";
import axios from "axios";
import SearchInput from "./SearchInput";
import CodeBlock from "./CodeBlock";

export const fetchSearchData = async (postData: { query: string }) => {
  const response = await axios.post("http://localhost:5000/query", {
    prompt: postData.query,
  });

  // axios automatically throws on non-2xx responses
  return response.data;
};

export default function SearchPanel() {
  const [prompt, setPrompt] = useState("");
  const [submittedPrompt, setSubmittedPrompt] = useState("");
  
  const postData = {
    query: submittedPrompt,
  }

  const { data , isFetching, isLoading } = useQuery({
    queryKey: ["search", submittedPrompt],
    queryFn: () => fetchSearchData(postData),
    enabled: !!submittedPrompt, // only run after enter
  });

  // 🔹 Dynamic columns from API response
  const columns = useMemo(() => {
    if (!data?.data.length) return [];
    return Object.keys(data.data[0]).map((key) => ({
      accessorKey: key,
      header: key.toUpperCase(),
    }));
  }, [data]);

  return (
    <Box sx={{ p: 2 }}>
      {/* Full width search */}
      <SearchInput
        disabled={isFetching}
        onSubmit={setSubmittedPrompt}
      />

      {data?.generated_sql && (
        <CodeBlock code={data.generated_sql} />
      )}

      {/* Table */}
      <Box sx={{ mt: 3 }}>
        <MaterialReactTable
          columns={columns}
          data={data?.data || []}
          state={{ isLoading }}
          initialState={{ density: "compact" }}
          enableFilters={false}
          enableExpandAll={false}
          enableColumnActions={false}
          enableColumnFilters={false}
        />
      </Box>
    </Box>
  );
}
