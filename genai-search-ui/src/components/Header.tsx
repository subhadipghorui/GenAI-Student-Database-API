import { AppBar, Toolbar, Typography, Box } from "@mui/material";

export default function Header() {
  return (
    <AppBar position="static" elevation={1} sx={{bgcolor: "#151d35ff", color: "#ffff"}}>
      <Toolbar>
        <Typography variant="h6" component="div">
          GenAI Search
        </Typography>

        <Box sx={{ flexGrow: 1 }} />

        <Typography variant="body2">
          v1.0
        </Typography>
      </Toolbar>
    </AppBar>
  );
}
