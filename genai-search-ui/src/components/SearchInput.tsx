import { FormControl, OutlinedInput } from "@mui/material";
import { useState, memo } from "react";

const SearchInput = memo(({ onSubmit, disabled }: {onSubmit: (value: string) => void, disabled?: boolean}) => {
  const [value, setValue] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmed = value.trim();
    if (trimmed) onSubmit(trimmed);
  };

  return (
    <form onSubmit={handleSubmit}>
      <FormControl fullWidth>
        <OutlinedInput
          placeholder="Search..."
          value={value}
          disabled={disabled}
          onChange={(e) => setValue(e.target.value)}
        />
      </FormControl>
    </form>
  );
});

export default SearchInput;
