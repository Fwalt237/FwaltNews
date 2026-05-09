import { useState } from 'react';

export default function TagInput({ tags = [], onChange, placeholder = 'Add tag…' }) {
  const [input, setInput] = useState('');

  const add = val => {
    const trimmed = val.trim().toLowerCase();
    if (trimmed && !tags.includes(trimmed)) onChange([...tags, trimmed]);
    setInput('');
  };

  const remove = tag => onChange(tags.filter(t => t !== tag));

  const onKey = e => {
    if (['Enter', ',', ' '].includes(e.key)) {
      e.preventDefault();
      if (input.trim()) add(input);
    }
    if (e.key === 'Backspace' && !input && tags.length) {
      remove(tags[tags.length - 1]);
    }
  };

  return (
    <div className="tag-input-wrap" onClick={() => document.getElementById('tag-inner')?.focus()}>
      {tags.map(t => (
        <span key={t} className="tag-chip">
          {t}
          <button type="button" onClick={e => { e.stopPropagation(); remove(t); }}>×</button>
        </span>
      ))}
      <input
        id="tag-inner"
        className="tag-input-inner"
        value={input}
        placeholder={tags.length === 0 ? placeholder : ''}
        onChange={e => setInput(e.target.value)}
        onKeyDown={onKey}
        onBlur={() => { if (input.trim()) add(input); }}
      />
    </div>
  );
}
