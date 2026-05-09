export default function Pagination({ current, total, onChange }) {
  if (total <= 1) return null;

  const pages = [];
  const delta = 1;
  const left  = current - delta;
  const right = current + delta;

  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= left && i <= right)) {
      pages.push(i);
    }
  }

  const withDots = [];
  let prev = null;
  for (const p of pages) {
    if (prev && p - prev > 1) withDots.push('…');
    withDots.push(p);
    prev = p;
  }

  return (
    <div className="pagination-wrap">
      <button className="page-btn" disabled={current === 1} onClick={() => onChange(current - 1)}>‹</button>
      {withDots.map((p, i) =>
        p === '…'
          ? <span key={`dots-${i}`} className="page-dots">…</span>
          : <button key={p} className={`page-btn${p === current ? ' active' : ''}`} onClick={() => onChange(p)}>{p}</button>
      )}
      <button className="page-btn" disabled={current === total} onClick={() => onChange(current + 1)}>›</button>
    </div>
  );
}
