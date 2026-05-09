import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { login, clearAuthError } from '../store/store';
import { FcGoogle } from 'react-icons/fc';
import { FaGithub } from 'react-icons/fa';
import { AiOutlineEye, AiOutlineEyeInvisible } from 'react-icons/ai';

export default function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, token } = useSelector(s => s.auth);

  const [form, setForm]     = useState({ username: '', password: '' });
  const [errors, setErrors] = useState({});
  const [show, setShow]     = useState(false);


  useEffect(() => { if (token) navigate('/v1/news'); }, [token, navigate]);
  useEffect(() => () => dispatch(clearAuthError()), [dispatch]);

  const validate = () => {
    const e = {};
    if (!form.username.trim()) e.username = 'Username is required';
    if (!form.password)        e.password = 'Password is required';
    return e;
  };

  const submit = e => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    dispatch(login(form));
  };

  const set = (k, v) => { setForm(f => ({ ...f, [k]: v })); setErrors(e => ({ ...e, [k]: '' })); };

  return (
    <div className="auth-page">
      <div className="auth-left">
        
        <div>
          <span className="auth-left-logo">Fwalt</span><span className="auth-left-logo-blue">News</span>
        </div>
        <h1 className="auth-left-title">Discover stories and share your voice freely</h1>
        <p className="auth-left-sub">Real-time news aggregation powered by a robust Spring Boot backend</p>
      </div>

      <div className="auth-right">
        <h2 className="auth-right-title">Sign in</h2>
        <p className="auth-right-sub">Welcome back — enter your credentials below</p>

        <form className="auth-form" onSubmit={submit} noValidate>
          {error && <div className="alert-error">⚠ {error}</div>}

          <div className="field">
            <label>Username</label>
            <input className={`field-input${errors.username ? ' error' : ''}`}
              type="text" value={form.username} autoComplete="username"
              placeholder="your_username"
              onChange={e => set('username', e.target.value)} />
            {errors.username && <span className="field-error">⚠ {errors.username}</span>}
          </div>

          <div className="field">
            <label>Password</label>
            <div style={{ position: 'relative' }}>
              <input className={`field-input${errors.password ? ' error' : ''}`}
                type={show ? 'text' : 'password'}
                value={form.password} autoComplete="current-password"
                placeholder="••••••••"
                onChange={e => set('password', e.target.value)}
                style={{ paddingRight: 44 }} />
               <button type="button" onClick={() => setShow(s => !s)}>
                  {show ? <AiOutlineEyeInvisible size={20} /> : <AiOutlineEye size={20} />}
               </button>
            </div>
            {errors.password && <span className="field-error">⚠ {errors.password}</span>}
          </div>

          <button type="submit" className="btn-red lg" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
            {loading ? <><span className="spinner" style={{width:15,height:15}}/> Signing in…</> : 'Login'}
          </button>
        </form>

        <div className="auth-divider">Or</div>

        <div className="auth-oauth">
          <a href="http://localhost:8080/oauth2/authorization/google"
            className="btn-oauth">
            <FcGoogle size={20} style={{ marginRight: 8 }} /> Sign up with Google
          </a>
          <a href="http://localhost:8080/oauth2/authorization/github"
            className="btn-oauth">
            <FaGithub size={20} style={{ marginRight: 8 }} /> Sign up with Github
          </a>
        </div>

        <p className="auth-footer-link">Don't have an account yet? <Link to="/v1/auth/signup">Sign up</Link></p>
        <p className="auth-terms">
          By signing up, I am agreeing to the{' '}
          <Link to="/about#tos" className="contact-link">Terms of Service</Link> 
          {' '}and{' '}
          <Link to="/about#pp" className="contact-link">Privacy Policy</Link>.
        </p>
      </div>
    </div>
  );
}
