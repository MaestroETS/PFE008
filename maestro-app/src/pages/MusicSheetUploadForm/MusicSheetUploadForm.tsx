import React from "react";
import {
  Box,
  Container,
  Divider,
  FormLabel,
  Grid,
  IconButton,
  Switch,
  TextField,
  Typography,
} from "@mui/material";
import { useForm, Controller, useFieldArray } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { MAX_TEMPO, MIN_TEMPO, formInputSchema } from "../../types/FormInput";
import MusicSheetUploader from "../../components/musicSheetUploader/MusicSheetUploader";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "../../components/languageSwitcher/LanguageSwitcher";
import useMaestroClient from "../../hooks/useMaestroClient";
import MusicSheetUploadFormFooter from "./MusicSheetUploadFormFooter";
import MusicSheetUploadFormHeader from "./MusicSheetUploadFormHeader";
import RoundedBox from "../../components/roundedBox/RoundedBox";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import Helper from "../../components/Helper/Helper";

const DEFAULT_FALLBACK_TEMPO = 120;

const MusicSheetUploadForm: React.FC = () => {
  const { t } = useTranslation("musicSheetUploadForm");

  const { convert, loading, error } = useMaestroClient();

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
      tempos: [{ tempo: DEFAULT_FALLBACK_TEMPO, measure: 1, force: false }],
      file: null,
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: "tempos",
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
    formData.append("file", data.file);
    formData.append("tempos", JSON.stringify(data.tempos));

    await convert(formData);
  };

  return (
    <Container maxWidth="md">
      <Box display="flex" justifyContent="flex-end" width="100%">
        <Helper />
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
        <Box display="flex" flexDirection="column" marginBottom={2}>
          <Box marginBottom={1}>
            <FormLabel>{t("MidiLabel")}</FormLabel>
          </Box>
          <Controller
            name="midiFileName"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                id="outlined-required"
                size="small"
                error={!!errors.midiFileName}
                helperText={
                  errors.midiFileName ? t(`${errors.midiFileName.message}`) : ""
                }
              />
            )}
          />
        </Box>
        <Grid container spacing={1} marginBottom={1}>
          <Grid item xs={4}>
            <FormLabel>{t("TempoLabel")}</FormLabel>
          </Grid>
          <Grid item xs={4}>
            <FormLabel>{t("MesureLabel")}</FormLabel>
          </Grid>
          <Grid item xs={2}>
            <FormLabel>{t("ForceTempoLabel")}</FormLabel>
          </Grid>
          <Grid item xs={2}>
            <FormLabel>{t("AddOrRemoveLabel")}</FormLabel>
          </Grid>
        </Grid>
        {fields.map((item, index) => (
          <Grid container spacing={1} key={item.id} marginBottom={2}>
            <Grid item xs={4}>
              <Controller
                name={`tempos.${index}.tempo`}
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    type="number"
                    id="outlined-required"
                    size="small"
                    error={!!errors.tempos?.[index]?.tempo}
                    helperText={
                      errors.tempos?.[index]?.tempo
                        ? t(`${errors.tempos[index]?.tempo?.message}`, {
                            minTempo: MIN_TEMPO,
                            maxTempo: MAX_TEMPO,
                          })
                        : ""
                    }
                  />
                )}
              />
            </Grid>
            <Grid item xs={4}>
              <Controller
                name={`tempos.${index}.measure`}
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    type="number"
                    id="outlined-required"
                    size="small"
                    error={!!errors.tempos?.[index]?.measure}
                    helperText={
                      errors.tempos?.[index]?.measure
                        ? t(`${errors.tempos[index]?.measure?.message}`)
                        : ""
                    }
                  />
                )}
              />
            </Grid>
            <Grid item xs={2} alignContent="center">
              <Controller
                name={`tempos.${index}.force`}
                control={control}
                render={({ field }) => <Switch {...field} />}
              />
            </Grid>
            <Grid item xs={2}>
              <IconButton
                aria-label="remove tempo"
                onClick={() => remove(index)}
                disabled={fields.length === 1}
              >
                <RemoveIcon />
              </IconButton>
              <IconButton
                aria-label="add tempo"
                onClick={() =>
                  append({
                    tempo: DEFAULT_FALLBACK_TEMPO,
                    measure: fields.length + 1,
                  })
                }
              >
                <AddIcon />
              </IconButton>
            </Grid>
          </Grid>
        ))}
        {loading && (
          <Box width="100%">
            <Divider sx={{ my: 2 }} />
            <Typography>{t("ConvertLoading")}</Typography>
          </Box>
        )}
        {!loading && error !== null && (
          <Box width="100%">
            <Divider sx={{ my: 2 }} />
            <Typography color="red">{t("ConvertError")}</Typography>
          </Box>
        )}
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
