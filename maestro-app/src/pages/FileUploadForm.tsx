import React, { useState } from "react";
import { Box, Button, Container, Divider, Typography } from "@mui/material";
import { styled } from "@mui/system";
import FileUploader from "../components/fileUploader/FileUploader";

const RoundedBox = styled(Box)({
  border: "2px solid #000",
  borderRadius: "15px",
  padding: "20px",
  marginTop: "20px",
  position: "relative",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
});

const UploadButton = styled(Button)({
  position: "absolute",
  bottom: "10px",
  right: "10px",
});

const FileUploadForm: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" align="center" gutterBottom>
        File Upload Form
      </Typography>
      <RoundedBox>
        <FileUploader onFileChange={setFile} title="Import your music sheet" />
        <Divider />
        <UploadButton variant="contained" color="primary" disabled={!file}>
          Upload
        </UploadButton>
      </RoundedBox>
    </Container>
  );
};

export default FileUploadForm;
