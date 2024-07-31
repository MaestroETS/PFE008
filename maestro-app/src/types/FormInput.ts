import * as yup from 'yup';

export const MIN_PAGE_START = 1;
export const MIN_TEMPO = 24;
export const MAX_TEMPO = 400;

export const formInputSchema = yup.object().shape({
  midiFileName: yup.string().required("Validation.MidiFileNameRequired"),
  tempos: yup.array().of(
    yup.object().shape({
      tempo: yup.number()
        .min(MIN_TEMPO, "Validation.TempoMin")
        .max(MAX_TEMPO, "Validation.TempoMax")
        .integer(),
      measure: yup.number()
        .min(1, "Validation.MeasureMin")
        .integer()
        .required("Validation.MeasureRequired"),
      force : yup.boolean()
    })
  ).required("Validation.TemposRequired"),
  file: yup.mixed(),
});

export type FormInput = {
  midiFileName: string;
  tempos: Array<{
    tempo: number;
    measure: number;
    force: boolean;
  }>;
  file: File | null;
};
