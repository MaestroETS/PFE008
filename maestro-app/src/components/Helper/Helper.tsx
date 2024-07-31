import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Box,
  IconButton,
  Tooltip,
  Modal,
  Typography,
  Grid,
} from "@mui/material";
import {
  Help,
  LooksOneOutlined,
  LooksTwoOutlined,
  Looks3Outlined,
  Looks4Outlined,
  Looks5Outlined,
  Looks6Outlined,
} from "@mui/icons-material";

const Helper: React.FC = () => {
  const { t } = useTranslation("components");
  const [open, setOpen] = useState(false);

  const showHelpModal = () => {
    setOpen(true);
  };

  const closeModal = () => {
    setOpen(false);
  };

  const modalStyle = {
    position: "absolute" as "absolute",
    top: "25%",
    left: "50%",
    transform: "translate(-50%, -25%)",
    width: "90%",
    maxWidth: "md",
    bgcolor: "background.paper",
    boxShadow: 24,
    p: 4,
    borderRadius: 3,
  };

  const textStyle = {
    wordWrap: "break-word",
    overflowWrap: "break-word",
    whiteSpace: "normal",
  };

  const steps = [
    {
      icon: <LooksOneOutlined style={{ color: "#1976d2" }} />,
      text: t("Helper.StepOne"),
    },
    {
      icon: <LooksTwoOutlined style={{ color: "#1976d2" }} />,
      text: t("Helper.StepTwo"),
    },
    {
      icon: <Looks3Outlined style={{ color: "#1976d2" }} />,
      text: t("Helper.StepThree"),
    },
    {
      icon: <Looks4Outlined style={{ color: "#1976d2" }} />,
      text: t("Helper.StepFour"),
    },
    {
      icon: <Looks5Outlined style={{ color: "#1976d2" }} />,
      text: t("Helper.StepFive"),
    },
    {
      icon: <Looks6Outlined style={{ color: "#1976d2" }} />,
      text: t("Helper.StepSix"),
    },
  ];

  return (
    <Box>
      <Tooltip title={t("Helper.Tooltip")}>
        <IconButton onClick={showHelpModal}>
          <Help style={{ color: "#1976d2" }} />
        </IconButton>
      </Tooltip>
      <Modal open={open} onClose={closeModal}>
        <Box sx={modalStyle}>
          <Typography variant="h5" gutterBottom>
            {t("Helper.Title")}
          </Typography>
          <Box
            border={1}
            borderColor={"lightgray"}
            borderRadius={3}
            padding={1}
          >
            <img src="/resources/helper.png" width={"100%"} alt="Maestro" />
          </Box>
          {steps.map((step, index) => (
            <Grid
              container
              alignContent="center"
              key={index}
              spacing={1}
              sx={{ mt: 1 }}
            >
              <Grid item xs={1}>
                {step.icon}
              </Grid>
              <Grid item xs={11}>
                <Typography sx={textStyle}>{step.text}</Typography>
              </Grid>
            </Grid>
          ))}
        </Box>
      </Modal>
    </Box>
  );
};

export default Helper;
