import { styled } from '@mui/system';
import { Box } from '@mui/material';

export const FileInput = styled('input')({
  display: 'none',
});

export const FileInputLabel = styled('label')(({ theme, disabled }: { theme: any, disabled?: boolean }) => ({
  cursor: disabled ? 'not-allowed' : 'pointer',
  backgroundColor: disabled ? theme.palette.action.disabledBackground : theme.palette.primary.main,
  color: disabled ? theme.palette.action.disabled : theme.palette.primary.contrastText,
  padding: '0.625em 1.25em',
  borderRadius: '0.3125em',
  '&:hover': {
    backgroundColor: disabled ? theme.palette.action.disabledBackground : theme.palette.primary.dark,
  },
}));

export const FileInputBox = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  border: '0.125em dashed',
  borderColor: theme.palette.primary.main,
  borderRadius: '0.625em',
  padding: '1.25em',
}));

export const CenteredBox = styled(Box)(({ theme }) => ({
  display:"flex",
  alignItems:"center",
  flexDirection: "column",
  paddingBottom: "1em"
}));