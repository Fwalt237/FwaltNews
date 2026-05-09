import { useSelector } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';

export default function ProtectedRoute({ children, roles }) {
  const { user, token } = useSelector(s => s.auth);
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const hasTokenInUrl = params.has('token');

  if (!token && !hasTokenInUrl) {
    return <Navigate to="/v1/auth/login" state={{ from: location }} replace />;
  }

  if (!user && hasTokenInUrl) {
    return (
      <div className="loading-center">
        <span className="spinner" />
        Verifying session...
      </div>
    );
  }

  if (roles && !roles.some(r => user?.roles?.includes(r))) {
    return <Navigate to="/v1/news" replace />;
  }

  return children;
}