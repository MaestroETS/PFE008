import React from "react";
import { Box, BoxProps } from "@mui/material";

const RoundedBox: React.FC<BoxProps> = (props) => {
  return (
    <Box
      {...props}
      sx={{
        borderRadius: "0.9375em",
        padding: "1.25em",
        position: "relative",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
        boxShadow: 4,
      }}
    />
  );
};

export default RoundedBox;
