import { ReactNode } from 'react';
import { Button, ButtonProps, CircularProgress } from '@mui/material';

interface LoadingButtonProps extends ButtonProps {
  loading?: boolean;
  loadingText?: string;
  startIcon?: ReactNode;
}

/**
 * Reusable button component with loading state
 */
export const LoadingButton = ({
  loading = false,
  loadingText,
  children,
  startIcon,
  disabled,
  ...props
}: LoadingButtonProps) => {
  const displayText = loading && loadingText ? loadingText : children;
  const displayIcon = loading ? (
    <CircularProgress size={20} />
  ) : (
    startIcon
  );

  return (
    <Button
      {...props}
      disabled={disabled || loading}
      startIcon={displayIcon}
    >
      {displayText}
    </Button>
  );
};

