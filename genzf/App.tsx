import React from 'react';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppProvider, useApp } from './context/AppContext';
import { Layout } from './components/Layout';
import { AuthLayout } from './layouts/AuthLayout';

// Pages
import { Home } from './pages/Home';
import { Markets } from './pages/Markets';
import { MarketDetail } from './pages/MarketDetail';
import { Calculators } from './pages/Calculators';
import { Portfolio } from './pages/Portfolio';
import { IncomeSplit } from './pages/IncomeSplit';

// Auth Pages
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { ForgotPassword } from './pages/auth/ForgotPassword';

// Protected Route Wrapper
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { user } = useApp();
    if (!user) return <Navigate to="/auth/login" replace />;
    return <>{children}</>;
};

// Guest Route Wrapper (redirects to dashboard if already logged in)
const GuestRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { user } = useApp();
    if (user) return <Navigate to="/portfolio" replace />;
    return <>{children}</>;
};

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Main Application Layout */}
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="markets" element={<Markets />} />
        <Route path="markets/:id" element={<MarketDetail />} />
        <Route path="calculators">
            <Route index element={<Navigate to="profit" replace />} />
            <Route path="profit" element={<Calculators type="profit" />} />
            <Route path="tax" element={<Calculators type="tax" />} />
        </Route>
        <Route 
            path="portfolio" 
            element={
                <ProtectedRoute>
                    <Portfolio />
                </ProtectedRoute>
            } 
        />
        <Route 
            path="income-split" 
            element={
                <ProtectedRoute>
                    <IncomeSplit />
                </ProtectedRoute>
            } 
        />
      </Route>

      {/* Authentication Layout */}
      <Route path="/auth" element={<GuestRoute><AuthLayout /></GuestRoute>}>
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="forgot-password" element={<ForgotPassword />} />
        <Route index element={<Navigate to="login" replace />} />
      </Route>

      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <AppProvider>
      <HashRouter>
        <AppRoutes />
      </HashRouter>
    </AppProvider>
  );
};

export default App;