import { useState } from 'react';
import { useDispatch } from 'react-redux';
import { deleteNews, fetchNews } from '../store/store';

export default function DeleteModal({ open, onClose, news, searchParams }) {
  const dispatch  = useDispatch();
  const [busy, setBusy] = useState(false);
  const [err,  setErr]  = useState('');

  const confirm = async () => {
    setBusy(true);
    setErr('');
    try {
      await dispatch(deleteNews(news.id)).unwrap();
      await dispatch(fetchNews(searchParams));
      onClose();
    } catch (e) {
      setErr(e || 'Delete failed');
    } finally {
      setBusy(false);
    }
  };

  if (!open || !news) return null;

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal-box" style={{ maxWidth: 420 }}>
        <div className="modal-header">
          <span className="modal-title">Delete News</span>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          <div className="delete-warning">
            <div className="icon">🗑️</div>
            <p>Are you sure you want to delete</p>
            <p><strong>"{news.title}"</strong>?</p>
            <p style={{ marginTop: 8, fontSize: 12, color: 'var(--text-muted)' }}>This action cannot be undone.</p>
          </div>
          {err && <div className="alert-error">⚠ {err}</div>}
        </div>
        <div className="modal-footer">
          <button className="btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button className="btn-red" onClick={confirm} disabled={busy}
            style={{ background: 'var(--red)' }}>
            {busy ? <><span className="spinner" style={{width:14,height:14}}/> Deleting…</> : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  );
}
