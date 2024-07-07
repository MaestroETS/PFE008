import * as yup from 'yup';

export const MIN_TEMPO = 40;
export const MAX_TEMPO = 240;

export const formInputSchema = yup.object().shape({
  midiFileName: yup.string().required("Validation.MidiFileNameRequired"),
  tempo: yup.number()
    .nullable()
    .min(MIN_TEMPO, "Validation.TempoMin")
    .max(MAX_TEMPO, "Validation.TempoMax")
    .transform((value) => (isNaN(value) ? undefined : value))
    .integer()
    .optional(),
  ignoreFirstPage: yup.boolean(),
  file: yup.mixed(),
});

export type FormInput = {
  midiFileName: string;
  tempo: number | null;
  ignoreFirstPage: boolean;
  file: File | null;
};
