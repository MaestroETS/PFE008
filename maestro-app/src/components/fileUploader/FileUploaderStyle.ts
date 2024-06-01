import { styled } from '@mui/system';

export const FileInput = styled('input')({
  display: 'none',
});

export const FileInputLabel = styled('label')(({ theme }) => ({
  cursor: 'pointer',
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  padding: '0.625em 1.25em',
  borderRadius: '0.3125em',
  border: '0.0625em solid #ccc',
  marginBottom: '1.25em',
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
  },
}));