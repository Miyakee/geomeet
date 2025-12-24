import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import JoinSessionPage from '../pages/JoinSessionPage';
import SessionPage from '../pages/SessionPage';
import ProtectedRoute from '../components/ProtectedRoute';

/**
 * Application routes configuration
 */
export const AppRoutes = () => {
  return (
    <Routes>
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route
        path={ROUTES.DASHBOARD}
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path={ROUTES.JOIN}
        element={
          <ProtectedRoute>
            <JoinSessionPage />
          </ProtectedRoute>
        }
      />
      <Route
        path={`${ROUTES.SESSION(':sessionId')}`}
        element={
          <ProtectedRoute>
            <SessionPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
    </Routes>
  );
};

