import React from "react";
import {
  Box,
  Checkbox,
  Container,
  Divider,
  FormControlLabel,
  FormLabel,
  TextField,
  Typography,
} from "@mui/material";
import { useForm, Controller } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { formInputSchema } from "../../types/FormInput";
import MusicSheetUploader from "../../components/musicSheetUploader/MusicSheetUploader";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "../../components/languageSwitcher/LanguageSwitcher";
import useMaestroClient from "../../hooks/useMaestroClient";
import MusicSheetUploadFormFooter from "./MusicSheetUploadFormFooter";
import MusicSheetUploadFormHeader from "./MusicSheetUploadFormHeader";
import RoundedBox from "../../components/roundedBox/RoundedBox";

const MusicSheetUploadForm: React.FC = () => {
  const { t } = useTranslation("musicSheetUploadForm");

  const { convert, loading } = useMaestroClient();

  const {
    control,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isDirty },
  } = useForm({
    resolver: yupResolver(formInputSchema),
    defaultValues: {
      midiFileName: "",
      tempo: 0,
      ignoreFirstPage: false,
      file: null,
    },
  });

  const file = watch("file");

  const canResetForm = (!isDirty && !file) || loading;

  const onFileChange = (file: File | null) => {
    setValue("file", file);
    if (file && file.name) {
      setValue("midiFileName", file.name.split(".")[0]);
    } else {
      setValue("midiFileName", "");
    }
  };

  const onSubmit = async (data: any) => {
    const formData = new FormData();
    formData.append("midiFileName", data.midiFileName);
    formData.append("ignoreFirstPage", data.ignoreFirstPage.toString());
    if (data.file) {
      formData.append("file", data.file);
    }

    await convert(formData);
  };

  return (
    <Container maxWidth="md">
      <Box display="flex" justifyContent="flex-end" width="100%">
        <LanguageSwitcher />
      </Box>
      <RoundedBox component={"form"} onSubmit={handleSubmit(onSubmit)}>
        <Box display="flex" marginBottom={"1.25em"}>
          <MusicSheetUploadFormHeader />
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
              error={!!errors.midiFileName}
              helperText={
                errors.midiFileName ? errors.midiFileName.message : ""
              }
            />
          )}
        />
        <FormLabel>{t("TempoLabel")}</FormLabel>
        <Controller
          name="tempo"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              type="number"
              required
              id="outlined-required"
              size="small"
              error={!!errors.tempo}
              helperText={errors.tempo ? errors.tempo.message : ""}
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
          <MusicSheetUploadFormFooter
            canResetForm={canResetForm}
            file={file}
            loading={loading}
            reset={reset}
          />
        </Box>
      </RoundedBox>
    </Container>
  );
};

export default MusicSheetUploadForm;
