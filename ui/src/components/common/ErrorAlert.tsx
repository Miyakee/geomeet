import { Alert, AlertProps } from '@mui/material';

interface ErrorAlertProps {
  message: string | null;
  severity?: AlertProps['severity'];
  onClose?: () => void;
}

/**
 * Reusable error alert component
 */
export const ErrorAlert = ({
  message,
  severity = 'error',
  onClose,
}: ErrorAlertProps) => {
  if (!message) {
    return null;
  }

  return (
    <Alert severity={severity} sx={{ width: '100%', mb: 2 }} onClose={onClose}>
      {message}
    </Alert>
  );
};

