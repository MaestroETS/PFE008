import React from "react";
import { Button } from "@mui/material";
import LoadingButton from "@mui/lab/LoadingButton";
import { useTranslation } from "react-i18next";

interface MusicSheetUploadFormFooterProps {
  canResetForm: boolean;
  file: File | null;
  loading: boolean;
  reset: () => void;
}

const MusicSheetUploadFormFooter: React.FC<MusicSheetUploadFormFooterProps> = ({
  canResetForm,
  file,
  loading,
  reset,
}) => {
  const { t } = useTranslation("musicSheetUploadForm");

  return (
    <>
      <Button
        variant="contained"
        color="primary"
        sx={{ mr: 1 }}
        onClick={() => reset()}
        disabled={canResetForm}
      >
        {t("Reset")}
      </Button>
      <LoadingButton
        variant="contained"
        color="primary"
        disabled={!file}
        type="submit"
        loading={loading}
      >
        {t("ConvertNow")}
      </LoadingButton>
    </>
  );
};

export default MusicSheetUploadFormFooter;
