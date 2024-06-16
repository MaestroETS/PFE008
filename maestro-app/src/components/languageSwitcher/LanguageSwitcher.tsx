import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Box, IconButton, Tooltip } from "@mui/material";
import { Language } from "@mui/icons-material";

const LanguageSwitcher: React.FC = () => {
  const { i18n } = useTranslation();
  const [currentLanguage, setCurrentLanguage] = useState(i18n.language);

  const toggleLanguage = () => {
    const newLanguage = currentLanguage === "en-US" ? "fr-CA" : "en-US";
    i18n.changeLanguage(newLanguage);
    setCurrentLanguage(newLanguage);
  };

  return (
    <Box>
      <Tooltip title={currentLanguage === "en-US" ? "Français" : "English"}>
        <IconButton onClick={toggleLanguage}>
          <Language style={{ color: "#1976d2" }} />
        </IconButton>
      </Tooltip>
    </Box>
  );
};

export default LanguageSwitcher;
