import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { signup, clearAuthError } from '../store/store';
import { FcGoogle } from 'react-icons/fc';
import { FaGithub } from 'react-icons/fa';
import { AiOutlineEye, AiOutlineEyeInvisible } from 'react-icons/ai';

export default function SignupPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, token } = useSelector(s => s.auth);

  const [form, setForm]     = useState({ username: '', email: '', password: '', firstName: '', lastName: '' });
  const [errors, setErrors] = useState({});
  const [show, setShow]     = useState(false);

  useEffect(() => { if (token) navigate('/news'); }, [token, navigate]);
  useEffect(() => () => dispatch(clearAuthError()), [dispatch]);
 
  const validate = () => {
    const e = {};
    if (!form.firstName.trim())                      e.firstName = 'First name is required';
    else if (form.firstName.length < 3)               e.firstName  = 'First name must be at least 3 characters';
    if (!form.lastName.trim())                       e.lastName  = 'Last name is required';
    else if (form.lastName.length < 3)               e.lastName  = 'Last name must be at least 3 characters';
    if (!form.email.trim())                          e.email     = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email))      e.email     = 'Invalid email address';
    if (!form.username.trim())                       e.username  = 'Username is required';
    else if (form.username.length < 3)               e.username  = 'Username must be at least 3 characters';
    if (!form.password)                              e.password  = 'Password is required';
    else if (form.password.length < 4)               e.password  = 'Password must be at least 4 characters';
    return e;
  };

  const submit = e => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    dispatch(signup(form));
  };

  const set = (k, v) => { setForm(f => ({ ...f, [k]: v })); setErrors(e => ({ ...e, [k]: '' })); };

  return (
    <div className="auth-page">
      <div className="auth-left">
        <div>
          <span className="auth-left-logo">Fwalt</span><span className="auth-left-logo-blue">News</span>
        </div>
        <h1 className="auth-left-title">See The latest trending news</h1>
        <p className="auth-left-sub">Join thousands of readers and contributors, Get access to real-time news</p>
      </div>

      <div className="auth-right">
        <div style={{ marginBottom: 14 }}>
          <h2 className="auth-right-title">Create an account</h2>
          <p className="auth-right-sub">Get started for free</p>
        </div>

        <form className="auth-form" onSubmit={submit} noValidate>
          {error && <div className="alert-error">⚠ {error}</div>}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="field">
              <label>First name *</label>
              <input className={`field-input${errors.firstName ? ' error' : ''}`}
                value={form.firstName} placeholder="Jonh"
                onChange={e => set('firstName', e.target.value)} />
              {errors.firstName && <span className="field-error">⚠ {errors.firstName}</span>}
            </div>
            <div className="field">
              <label>Last name *</label>
              <input className={`field-input${errors.lastName ? ' error' : ''}`}
                value={form.lastName} placeholder="Doe"
                onChange={e => set('lastName', e.target.value)} />
              {errors.lastName && <span className="field-error">⚠ {errors.lastName}</span>}
            </div>
          </div>

          <div className="field">
            <label>Username *</label>
            <input className={`field-input${errors.username ? ' error' : ''}`}
              value={form.username} placeholder="john_doe"
              onChange={e => set('username', e.target.value)} />
            {errors.username && <span className="field-error">⚠ {errors.username}</span>}
          </div>

          <div className="field">
            <label>Email *</label>
            <input className={`field-input${errors.email ? ' error' : ''}`}
              type="email" value={form.email} placeholder="john@company.com"
              onChange={e => set('email', e.target.value)} />
            {errors.email && <span className="field-error">⚠ {errors.email}</span>}
          </div>

          <div className="field">
            <label>Password *</label>
            <div style={{ position: 'relative' }}>
              <input className={`field-input${errors.password ? ' error' : ''}`}
                type={show ? 'text' : 'password'}
                value={form.password} placeholder="Enter your password"
                onChange={e => set('password', e.target.value)}
                style={{ paddingRight: 44 }} />
              <button type="button" onClick={() => setShow(s => !s)}>
                {show ? <AiOutlineEyeInvisible size={20} /> : <AiOutlineEye size={20} />}
              </button>
            </div>
            {errors.password && <span className="field-error">⚠ {errors.password}</span>}
          </div>

          <button type="submit" className="btn-red lg" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
            {loading ? <><span className="spinner" style={{width:15,height:15}}/> Creating account…</> : 'Sign up'}
          </button>
        </form>

        <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
          <a href="http://localhost:8080/oauth2/authorization/google"
            className="btn-oauth">
            <FcGoogle size={20} style={{ marginRight: 8 }} /> Sign up with Google
          </a>
          <a href="http://localhost:8080/oauth2/authorization/github"
            className="btn-oauth">
            <FaGithub size={20} style={{ marginRight: 8 }} /> Sign up with Github
          </a>
        </div>

        <p className="auth-footer-link" style={{ marginTop: 16 }}>
          Already have an account? <Link to="/v1/auth/login">Log in</Link>
        </p>
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
