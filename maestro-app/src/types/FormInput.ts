import * as yup from 'yup';
import i18n from '../i18n/i18n';

export const formInputSchema = yup.object().shape({
  midiFileName: yup.string().required(i18n.t('midiFileNameRequired')),
  tempo: yup.number()
    .nullable()
    .required(i18n.t('tempoRequired'))
    .min(40, i18n.t('tempoMin'))
    .max(240, i18n.t('tempoMax'))
    .integer(),
  ignoreFirstPage: yup.boolean(),
  file: yup.mixed(),
});

export type FormInput = {
  midiFileName: string;
  tempo: number | null ;
  ignoreFirstPage: boolean;
  file: File | null;
};