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
import { useTranslation } from "react-i18next";

interface MusicSheetUploaderProps {
  onFileChange: (file: File | null) => void;
  file: File | null;
}

const MusicSheetUploader: React.FC<MusicSheetUploaderProps> = ({
  onFileChange,
  file,
}) => {
  const { t } = useTranslation("components");
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
            <Typography>{t("musicSheetUploader.DragAndDrop")}</Typography>
          </CenteredBox>
          <CenteredBox>
            <Typography>{t("musicSheetUploader.Or")}</Typography>
          </CenteredBox>
          <CenteredBox>
            <Typography gutterBottom>
              {t("musicSheetUploader.UploadYourMusicSheet")}
            </Typography>
            <FileInputLabel htmlFor="file-input" theme={undefined}>
              {t("musicSheetUploader.BrowseFiles")}
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
            {t("musicSheetUploader.AvailableFileExtensions")}
          </Typography>
        </>
      ) : (
        <>
          <CenteredBox>
            <Typography variant="h5">
              {t("musicSheetUploader.LetsGroove")}
            </Typography>
            <Typography variant="h5">
              {t("musicSheetUploader.YourMusicSheetIsMIDIR")}
            </Typography>
          </CenteredBox>
          <MusicNote style={{ fontSize: "6rem", color: "#1976d2" }} />
          <Box display="flex" alignItems="center">
            <Typography>{`${t("musicSheetUploader.UploadedFile")}${
              file.name
            }`}</Typography>
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
