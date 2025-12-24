import { ReactNode } from 'react';
import { Container, Box, Paper, ContainerProps } from '@mui/material';

interface PageContainerProps {
  children: ReactNode;
  maxWidth?: ContainerProps['maxWidth'];
  elevation?: number;
  padding?: number;
}

/**
 * Reusable page container component with consistent styling
 */
export const PageContainer = ({
  children,
  maxWidth = 'md',
  elevation = 3,
  padding = 4,
}: PageContainerProps) => {
  return (
    <Container component="main" maxWidth={maxWidth}>
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper
          elevation={elevation}
          sx={{
            padding,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
          }}
        >
          {children}
        </Paper>
      </Box>
    </Container>
  );
};

