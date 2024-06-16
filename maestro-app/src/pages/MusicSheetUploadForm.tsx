import React from "react";
import {
  Box,
  Button,
  Checkbox,
  Container,
  Divider,
  FormControlLabel,
  FormLabel,
  TextField,
  Typography,
} from "@mui/material";
import LoadingButton from "@mui/lab/LoadingButton";
import { styled } from "@mui/system";
import { useForm, Controller, SubmitHandler } from "react-hook-form";
import MusicSheetUploader from "../components/musicSheetUploader/MusicSheetUploader";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "../components/languageSwitcher/LanguageSwitcher";
import { FormInput } from "../types/FormInput";
import useMaestroClient from "../hooks/useMaestroClient";

const RoundedBox = styled(Box)({
  border: "0.125em solid gray",
  borderRadius: "0.9375em",
  padding: "1.25em",
  position: "relative",
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
});

const MusicSheetUploadForm: React.FC = () => {
  const { t } = useTranslation("musicSheetUploadForm");

  const { convert, loading, error } = useMaestroClient();

  const { control, handleSubmit, reset, setValue, watch, formState } =
    useForm<FormInput>({
      defaultValues: {
        midiFileName: "",
        ignoreFirstPage: false,
        file: null,
      },
    });

  const file = watch("file");

  const onFileChange = (file: File | null) => {
    setValue("file", file);
    if (file && file.name) {
      setValue("midiFileName", file.name.split(".")[0]);
    } else {
      setValue("midiFileName", "");
    }
  };

  const onSubmit: SubmitHandler<FormInput> = async (data) => {
    const formData = new FormData();
    formData.append("midiFileName", data.midiFileName);
    formData.append("ignoreFirstPage", data.ignoreFirstPage.toString());
    if (data.file) {
      formData.append("file", data.file);
    }
    await convert(formData);
  };

  const handleResetForm = () => {
    reset();
  };

  return (
    <Container maxWidth="md">
      <Box display="flex" justifyContent="flex-end" width="100%">
        <LanguageSwitcher />
      </Box>
      <RoundedBox component={"form"} onSubmit={handleSubmit(onSubmit)}>
        <Box display="flex" marginBottom={"1.25em"}>
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
        </Box>
        <Box width={"100%"}>
          <Controller
            name="file"
            control={control}
            render={({ field }) => (
              <MusicSheetUploader
                onFileChange={(file) => onFileChange(file)}
                file={field.value}
              />
            )}
          />
        </Box>
        <Box width={"100%"} paddingY={"1em"}>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h5">{t("OptionsTitle")}</Typography>
          <Typography variant="subtitle2" color={"gray"} gutterBottom>
            {t("OptionsSubTitle")}
          </Typography>
        </Box>
        <FormLabel>{t("MidiLabel")}</FormLabel>
        <Controller
          name="midiFileName"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              required
              id="outlined-required"
              size="small"
            />
          )}
        />
        <FormControlLabel
          control={
            <Controller
              name="ignoreFirstPage"
              control={control}
              render={({ field }) => (
                <Checkbox {...field} checked={field.value} />
              )}
            />
          }
          label={t("IgnoreLabel")}
        />
        <Divider sx={{ my: 2 }} />
        <Box display="flex" justifyContent="flex-end" width="100%">
          <Button
            variant="contained"
            color="primary"
            sx={{ mr: 1 }}
            onClick={handleResetForm}
            disabled={(!formState.isDirty && !file) || loading}
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
        </Box>
      </RoundedBox>
    </Container>
  );
};

export default MusicSheetUploadForm;
