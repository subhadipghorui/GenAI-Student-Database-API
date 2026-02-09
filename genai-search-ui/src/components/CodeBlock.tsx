import { Box, IconButton, Tooltip } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { memo } from "react";

const CodeBlock = memo(({ code }: {code: string}) => {
  const handleCopy = async () => {
    await navigator.clipboard.writeText(code);
  };

  return (
    <Box
      sx={{
        position: "relative",
        bgcolor: "#0f172a",
        color: "#e5e7eb",
        py: 2,
        px:4,
        my:2,
        borderRadius: 2,
        fontFamily: "monospace",
        fontSize: "0.875rem",
        whiteSpace: "pre-wrap",   // ✅ wrap lines
        wordBreak: "break-word",  // ✅ break long tokens
      }}
    >
      <Tooltip title="Copy">
        <IconButton
          size="small"
          onClick={handleCopy}
          sx={{
            position: "absolute",
            top: 8,
            right: 8,
            color: "#94a3b8",
          }}
        >
          <ContentCopyIcon fontSize="inherit" />
        </IconButton>
      </Tooltip>

      <pre
        style={{
          margin: 0,
          whiteSpace: "inherit",
          wordBreak: "inherit",
        }}
      >
        <code lang="sql">{code}</code>
      </pre>
    </Box>
  );
});

export default CodeBlock;
