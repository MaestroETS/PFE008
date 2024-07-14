import * as yup from 'yup';

const MIN_PAGE_START = 1

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
  shouldParsePageRange: yup.boolean(),
  file: yup.mixed(),
  pageRangeStart: yup.number()
  .min(MIN_PAGE_START, "Validation.PageRange")
  .nullable()
  .transform((value) => (isNaN(value) ? undefined : value))
  .integer()
  .optional(),
  pageRangeEnd: yup.number()
  .min(MIN_PAGE_START, "Validation.PageRange")
  .nullable()
  .transform((value) => (isNaN(value) ? undefined : value))
  .integer()
  .optional(),
});

export type FormInput = {
  midiFileName: string;
  tempo: number | null;
  shouldParsePageRange: boolean;
  file: File | null;
};
