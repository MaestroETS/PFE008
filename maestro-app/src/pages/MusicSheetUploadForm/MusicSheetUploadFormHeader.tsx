import React from "react";
import { Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

const MusicSheetUploadFormHeader: React.FC = () => {
  const { t } = useTranslation("musicSheetUploadForm");

  return (
    <>
      <Box marginRight={"1.25em"}>
        <img
          src="/resources/maestro.png"
          width={"75"}
          height={"75"}
          alt="Maestro"
        />
      </Box>
      <Box>
        <Typography variant="h3">{t("MaestroTitle")}</Typography>
        <Typography variant="subtitle2" color={"gray"}>
          {t("MaestroSubTitle")}
        </Typography>
      </Box>
    </>
  );
};

export default MusicSheetUploadFormHeader;
