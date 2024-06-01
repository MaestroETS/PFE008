import React, { useState } from "react";
import { Typography, Box } from "@mui/material";
import { FileInput, FileInputLabel } from "./FileUploaderStyle";

interface FileUploaderProps {
  onFileChange: (file: File | null) => void;
  title: string;
  disabled?: boolean;
}

const FileUploader: React.FC<FileUploaderProps> = ({
  onFileChange,
  title,
  disabled = false,
}) => {
  const [fileName, setFileName] = useState<string | null>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      const selectedFile = event.target.files[0];
      setFileName(selectedFile.name);
      onFileChange(selectedFile);
    } else {
      setFileName(null);
      onFileChange(null);
    }
  };

  return (
    <Box
      position="relative"
      display="flex"
      flexDirection="column"
      alignItems="center"
    >
      <FileInputLabel htmlFor="file-input">{title}</FileInputLabel>
      <FileInput
        id="file-input"
        type="file"
        onChange={handleFileChange}
        disabled={disabled}
      />
      {fileName && <Typography variant="body1">{fileName}</Typography>}
    </Box>
  );
};

export default FileUploader;
