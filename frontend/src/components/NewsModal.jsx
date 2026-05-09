import { useState, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { createNews, updateNews, fetchNews, clearCurrentNews  } from '../store/store';
import TagInput from './TagInput';

const EMPTY = { title: '', content: '', author: '', tags: [] };

export default function NewsModal({ open, onClose, editing, searchParams }) {
  const dispatch = useDispatch();
  const [form,   setForm]   = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [apiErr, setApiErr] = useState('');

  useEffect(() => {
    if (open) {
      setErrors({});
      setApiErr('');
      if (editing) {
        setForm({
          title:   editing.title ?? '',
          content: editing.content?.replace(/<[^>]*>/g, '') ?? '',
          author:  editing.authorDto?.name ?? '',
          tags:    editing.tagsDto?.map(t => t.name) ?? [],
        });
      } else {
        setForm(EMPTY);
      }
    }
  }, [open, editing]);

  const validate = () => {
    const e = {};
    if (!form.title.trim())                         e.title = 'Title is required';
    else if (form.title.length < 6)                 e.title = 'Title must be at least 6 characters';
    else if (form.title.length > 1000)              e.title = 'Title must be at most 1000 characters';
    if (!editing && !form.author.trim())            e.author = 'Author is required';
    else if (form.author.length < 3)                e.author = 'Author name must be at least 3 characters';
    if (!form.content.trim())                       e.content = 'Content is required';
    else if (form.content.length < 12)              e.content = 'Content must be at least 12 characters';
    if (form.tags.length === 0)                     e.tags = 'Please add at least one tag.';
    else if (form.tags.some(t => t.length < 3))     e.tags = 'Each tag must be at least 3 characters';
    else if (form.tags.some(t => t.length > 15))     e.tags = 'Each tag must be at most 15 characters';
    return e;
  };

  const submit = async () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setSaving(true);
    setApiErr('');
    try {
      if (editing) {
        await dispatch(updateNews({ id: editing.id, data: { title: form.title, content: form.content, author: form.author || undefined, tags: form.tags } })).unwrap();
        dispatch(clearCurrentNews());
      } else {
        await dispatch(createNews({ title: form.title, content: form.content, author: form.author, tags: form.tags })).unwrap();
      }
      await dispatch(fetchNews(searchParams));
      onClose();
    } catch (err) {
      const message = typeof err === 'string' 
        ? err 
        : (err?.message || 'Something went wrong');
      setApiErr(message);
    } finally {
      setSaving(false);
    }
  };

  if (!open) return null;
  const set = (k, v) => { setForm(f => ({ ...f, [k]: v })); setErrors(e => ({ ...e, [k]: '' })); };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal-box">
        <div className="modal-header">
          <span className="modal-title">{editing ? 'Edit News' : 'Add News'}</span>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          {apiErr && <div className="alert-error">⚠ {apiErr}</div>}

          <div className="field">
            <label>Title *</label>
            <input className={`field-input${errors.title ? ' error' : ''}`} value={form.title} onChange={e => set('title', e.target.value)} placeholder="Enter news title…" />
            {errors.title && <span className="field-error">⚠ {errors.title}</span>}
          </div>

          {!editing && (
            <div className="field">
              <label>Author *</label>
              <input className={`field-input${errors.author ? ' error' : ''}`} value={form.author} onChange={e => set('author', e.target.value)} placeholder="Author name…" />
              {errors.author && <span className="field-error">⚠ {errors.author}</span>}
            </div>
          )}

          <div className="field">
            <label>Tags</label>
            <TagInput tags={form.tags} onChange={v => set('tags', v)} placeholder="Type tag and press Enter…" />
              {errors.tags && <span className="field-error">⚠ {errors.tags}</span>}
          </div>

          <div className="field">
            <label>Content *</label>
            <textarea className={`field-input${errors.content ? ' error' : ''}`} value={form.content} onChange={e => set('content', e.target.value)} placeholder="Write article content…" rows={6} />
            {errors.content && <span className="field-error">⚠ {errors.content}</span>}
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn-ghost" onClick={onClose} disabled={saving}>Cancel</button>
          <button className="btn-red" onClick={submit} disabled={saving}>
            {saving ? <><span className="spinner" style={{width:14,height:14}} /> Saving…</> : (editing ? 'Save Changes' : 'Publish News')}
          </button>
        </div>
      </div>
    </div>
  );
}
