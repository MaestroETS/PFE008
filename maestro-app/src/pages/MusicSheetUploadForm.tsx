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
import { styled } from "@mui/system";
import { useForm, Controller, SubmitHandler } from "react-hook-form";
import MusicSheetUploader from "../components/musicSheetUploader/MusicSheetUploader";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "../components/languageSwitcher/LanguageSwitcher";
import convertToMidi from "../mocks/convertToMidiMock";
import { FormInput } from "../types/FormInput";

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

  const { control, handleSubmit, reset, setValue, watch, formState } =
    useForm<FormInput>({
      defaultValues: {
        midiFileName: "",
        ignoreFirstPage: false,
        file: null,
      },
    });
  const file = watch("file");

  const onSubmit: SubmitHandler<FormInput> = async (data) => {
    await convertToMidi();
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
        <Typography variant="h4">{t("MaestroTitle")}</Typography>
        <Typography variant="subtitle2" color={"gray"} gutterBottom>
          {t("MaestroSubTitle")}
        </Typography>
        <Box width={"100%"}>
          <Controller
            name="file"
            control={control}
            render={({ field }) => (
              <MusicSheetUploader
                onFileChange={(file) => setValue("file", file)}
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
            disabled={!formState.isDirty && !file}
          >
            {t("Reset")}
          </Button>
          <Button
            variant="contained"
            color="primary"
            disabled={!file}
            type="submit"
          >
            {t("ConvertNow")}
          </Button>
        </Box>
      </RoundedBox>
    </Container>
  );
};

export default MusicSheetUploadForm;
