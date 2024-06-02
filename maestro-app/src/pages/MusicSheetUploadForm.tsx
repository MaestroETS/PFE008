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

interface IFormInput {
  midiFileName: string;
  ignoreFirstPage: boolean;
  file: File | null;
}

const RoundedBox = styled(Box)({
  border: "2px solid gray",
  borderRadius: "15px",
  padding: "20px",
  marginTop: "20px",
  position: "relative",
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
});

const MusicSheetUploadForm: React.FC = () => {
  const { t } = useTranslation("musicSheetUploadForm");

  const { control, handleSubmit, reset, setValue, watch, formState } =
    useForm<IFormInput>({
      defaultValues: {
        midiFileName: "",
        ignoreFirstPage: false,
        file: null,
      },
    });
  const file = watch("file");

  const onSubmit: SubmitHandler<IFormInput> = (data) => {
    console.log(data);
  };

  const handleResetForm = () => {
    reset();
  };

  return (
    <Container maxWidth="md">
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
              render={({ field }) => <Checkbox {...field} />}
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
            disabled={!formState.isDirty}
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
