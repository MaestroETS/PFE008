import React, { useEffect, useRef } from "react";
import { Box, IconButton, Typography } from "@mui/material";
import { useDropzone } from "react-dropzone";
import { CloudUpload, MusicNote, Cancel } from "@mui/icons-material";
import {
  FileInput,
  FileInputLabel,
  FileInputBox,
  CenteredBox,
} from "./MusicSheetUploaderStyles";

interface MusicSheetUploaderProps {
  onFileChange: (file: File | null) => void;
  file: File | null;
}

const MusicSheetUploader: React.FC<MusicSheetUploaderProps> = ({
  onFileChange,
  file,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!file && fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  }, [file]);

  const onDrop = (acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      const selectedFile = acceptedFiles[0];
      onFileChange(selectedFile);
    }
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      const selectedFile = event.target.files[0];
      onFileChange(selectedFile);
    } else {
      onFileChange(null);
    }
  };

  const handleRemoveFile = () => {
    onFileChange(null);
  };

  const { getRootProps, getInputProps } = useDropzone({ onDrop });

  return (
    <FileInputBox {...getRootProps()}>
      {!file ? (
        <>
          <CenteredBox>
            <CloudUpload style={{ fontSize: "6rem", color: "#1976d2" }} />
            <Typography>Drag and drop</Typography>
          </CenteredBox>
          <CenteredBox>
            <Typography>Or</Typography>
          </CenteredBox>
          <CenteredBox>
            <Typography gutterBottom>Upload your music sheet!</Typography>
            <FileInputLabel htmlFor="file-input" theme={undefined}>
              Browse Files
            </FileInputLabel>
            <FileInput
              id="file-input"
              type="file"
              onChange={handleFileChange}
              ref={fileInputRef}
              {...getInputProps()}
            />
          </CenteredBox>
          <Typography variant="caption">
            (Available file extensions: png, jpeg, pdf)
          </Typography>
        </>
      ) : (
        <>
          <CenteredBox>
            <Typography variant="h5">Let's groove!</Typography>
            <Typography variant="h5">
              Your music sheet is MIDI-ready!
            </Typography>
          </CenteredBox>
          <MusicNote style={{ fontSize: "6rem", color: "#1976d2" }} />
          <Box display="flex" alignItems="center">
            <Typography>{`Uploaded file: ${file.name}`}</Typography>
            <IconButton onClick={handleRemoveFile} aria-label="remove-file">
              <Cancel />
            </IconButton>
          </Box>
        </>
      )}
    </FileInputBox>
  );
};

export default MusicSheetUploader;
