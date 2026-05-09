import { useDispatch, useSelector } from 'react-redux';
import { NavLink, useNavigate } from 'react-router-dom';
import { logout } from '../store/store';

export default function Header() {
  const dispatch   = useDispatch();
  const navigate   = useNavigate();
  const { user }   = useSelector(s => s.auth);
  const isAdmin    = user?.roles?.includes('ROLE_ADMIN');
  const isUser     = user?.roles?.includes('ROLE_USER') || isAdmin;

  const handleLogout = () => {
    dispatch(logout());
    navigate('/v1/auth/login');
  };

  return (
    <header className="app-header">
      <NavLink to="/" className="header-logo">
        <img src="/wordmark.jpg" alt="FwaltNews" className="header-logo-img" />
      </NavLink>

      <nav className="header-nav-hac">
        <NavLink to="/" className="nav-link-hac">HOME</NavLink>
        <NavLink to="/about" className="nav-link-hac">ABOUT</NavLink>
        <NavLink to="/contact" className="nav-link-hac">CONTACT</NavLink>
      </nav>

      <div className="header-actions">
        {user ? (
          <>
            <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>
              <span style={{ color: 'var(--accent-blue)', fontWeight: 500 }}>{user.username}</span>
              {isAdmin && <span className="tag-badge red" style={{ marginLeft: 8 }}>Admin</span>}
              {!isAdmin && isUser && <span className="tag-badge" style={{ marginLeft: 8 }}>User</span>}
            </span>
            <button className="btn-ghost sm" onClick={handleLogout}>Sign Out</button>
          </>
        ) : (
          <>
            <NavLink to="/v1/auth/login"  className="btn-ghost sm">Sign In</NavLink>
            <NavLink to="/v1/auth/signup" className="btn-red sm">Sign Up</NavLink>
          </>
        )}
      </div>
    </header>
  );
}
