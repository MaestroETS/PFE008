import React from "react";
import {
  Box,
  Checkbox,
  Container,
  Divider,
  FormControlLabel,
  FormLabel,
  Grid,
  IconButton,
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
      tempos: [{ tempo: undefined, measure: 1 }],
      shouldParsePageRange: false,
      file: null,
      pageRangeStart: null,
      pageRangeEnd: null,
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: "tempos",
  });

  const file = watch("file");
  const shouldParsePageRange = watch("shouldParsePageRange");

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
    formData.append("shouldParsePageRange", data.shouldParsePageRange);
    if (data.shouldParsePageRange) {
      formData.append("pageRangeStart", data.pageRangeStart);
      formData.append("pageRangeEnd", data.pageRangeEnd);
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
              id="outlined-required"
              size="small"
              error={!!errors.midiFileName}
              helperText={
                errors.midiFileName ? t(`${errors.midiFileName.message}`) : ""
              }
            />
          )}
        />
        <br />
        <FormLabel>{t("TempoLabel")}</FormLabel>
        {fields.map((item, index) => (
          <Grid container spacing={2} key={item.id}>
            <Grid item xs={5}>
              <Controller
                name={`tempos.${index}.tempo`}
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    type="number"
                    id="outlined-required"
                    size="small"
                    label={t("Tempo")}
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
            <Grid item xs={5}>
              <Controller
                name={`tempos.${index}.measure`}
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    type="number"
                    id="outlined-required"
                    size="small"
                    label={t("Measure")}
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
                onClick={() => append({ tempo: 120, measure: fields.length + 1 })}
              >
                <AddIcon />
              </IconButton>
            </Grid>
          </Grid>
        ))}
        <br />
        <FormControlLabel
          control={
            <Controller
              name="shouldParsePageRange"
              control={control}
              render={({ field }) => (
                <Checkbox {...field} checked={field.value} />
              )}
            />
          }
          label={t("ParsePageLabel")}
        />
        {shouldParsePageRange && (
          <Box width="100%" paddingY="1em">
            <Grid container spacing={1} alignItems="center">
              <Grid item xs={3}>
                <Controller
                  name="pageRangeStart"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      type="number"
                      id="outlined-required"
                      size="small"
                      label={t("StartPageLabel")}
                      error={!!errors.pageRangeStart}
                      helperText={
                        errors.pageRangeStart
                          ? t(`${errors.pageRangeStart.message}`)
                          : ""
                      }
                    />
                  )}
                />
              </Grid>
              <Grid item xs={3}>
                <Controller
                  name="pageRangeEnd"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      type="number"
                      id="outlined-required"
                      size="small"
                      label={t("EndPageLabel")}
                      error={!!errors.pageRangeEnd}
                      helperText={
                        errors.pageRangeEnd
                          ? t(`${errors.pageRangeEnd.message}`)
                          : ""
                      }
                    />
                  )}
                />
              </Grid>
            </Grid>
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
