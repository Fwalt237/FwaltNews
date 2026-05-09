import { useEffect, useState, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';
import { fetchNews, loginSuccess } from '../store/store';
import NewsCard    from '../components/NewsCard';
import NewsModal   from '../components/NewsModal';
import DeleteModal from '../components/DeleteModal';
import Pagination  from '../components/Pagination';


const PAGE_SIZES = [10, 20, 50];
const SORT_OPTIONS = [
  { label: 'Latest',  value: 'createdDate:DESC' },
  { label: 'Oldest',  value: 'createdDate:ASC'  },
  { label: 'Title ASC',     value: 'title:ASC' },
  { label: 'Title DESC',     value: 'title:DESC' },
];

function parseSearch(raw) {
  const tags    = [];
  const tagRe   = /#\(([^)]+)\)/g;
  let   m;
  while ((m = tagRe.exec(raw)) !== null) tags.push(m[1].trim());
  const text = raw.replace(tagRe, '').trim();
  return { text, tags };
}

export default function NewsPage() {
  const dispatch = useDispatch();
  const { items, currentPage, pageCount, loading, error } = useSelector(s => s.news);
  const { user } = useSelector(s => s.auth);
  const isAdmin  = user?.roles?.includes('ROLE_ADMIN');
  const isUser   = user?.roles?.includes('ROLE_USER') || isAdmin;

  const [searchParams, setSearchParams] = useSearchParams();

  const page     = parseInt(searchParams.get('page')     || '1', 10);
  const pageSize = parseInt(searchParams.get('pageSize') || '10', 10);
  const sortVal  = searchParams.get('sort') || 'createdDate:DESC';
  const query    = searchParams.get('q')    || '';

  const [rawQuery, setRawQuery] = useState(query);
  const [addOpen,  setAddOpen]  = useState(false);
  const [editing,  setEditing]  = useState(null);
  const [deleting, setDeleting] = useState(null);
  
  const buildApiParams = useCallback((p = page, ps = pageSize, sv = sortVal, q = query) => {
    const { text, tags } = parseSearch(q);
    const params = { page: p, pageSize: ps, sortByAndOrder: sv };
    const criteria = [];

    if (text) {
      criteria.push(`keyword:like:${text}`);
    }

    tags.forEach(t => {
      criteria.push(`tags.name:eq:${t}`);
    });

    if (criteria.length) params.searchCriteria = criteria;
    return params;
  }, [page, pageSize, sortVal, query]);

    useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
  
      if (token) {
        localStorage.setItem('token', token);
        dispatch(loginSuccess({ token })); 
        window.history.replaceState({}, document.title, "/v1/news");
      }
    }, [dispatch]);

  useEffect(() => {
    dispatch(fetchNews(buildApiParams()));
  }, [page, pageSize, sortVal, query, dispatch, buildApiParams]);

  const updateUrl = (updates) => {
    const next = new URLSearchParams(searchParams);
    Object.entries(updates).forEach(([k, v]) => {
      if (v !== undefined && v !== null) next.set(k, String(v));
      else next.delete(k);
    });
    setSearchParams(next, { replace: true });
  };

  const handleSearch = e => {
    e.preventDefault();
    updateUrl({ q: rawQuery, page: 1 });
  };

  const handleSort    = e  => updateUrl({ sort: e.target.value, page: 1 });
  const handlePageSz  = e  => updateUrl({ pageSize: e.target.value, page: 1 });
  const handlePage    = p  => updateUrl({ page: p });

  return (
    <div className="container-fluid" style={{ maxWidth: 1400, margin: '0 auto', padding: '0 24px' }}>
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1 }}>
          <div>
            <h1 className="page-title">Latest News</h1>
            <p className="page-sub">Stay informed with the latest stories</p>
          </div>
          {(isUser || isAdmin) && (
            <button className="btn-blue" onClick={() => setAddOpen(true)}>＋ Add News</button>
          )}
        </div>
      </div>

      <div className="toolbar">
        <form onSubmit={handleSearch} style={{ flex: 1, minWidth: 260, display: 'flex', gap: 8 }}>
          <div className="search-bar-wrap" style={{ flex: 1 }}>
            <span className="search-icon">🔍</span>
            <input
              className="field-input"
              value={rawQuery}
              onChange={e => setRawQuery(e.target.value)}
              placeholder='Search… e.g. climate #(energy) #(policy)'
            />
          </div>
          <button type="submit" className="btn-ghost">Search</button>
        </form>

        <select className="toolbar-select" value={sortVal} onChange={handleSort}>
          {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>

        <select className="toolbar-select" value={pageSize} onChange={handlePageSz}>
          {PAGE_SIZES.map(s => <option key={s} value={s}>{s} per page</option>)}
        </select>
      </div>

      {query && (
        <p className="search-hint" style={{ marginBottom: 16 }}>
          Showing results for <strong style={{ color: 'var(--text-primary)' }}>"{query}"</strong>
          &nbsp;<button className="btn-ghost sm" onClick={() => { setRawQuery(''); updateUrl({ q: null, page: 1 }); }}>✕ Clear</button>
        </p>
      )}

      {error && <div className="alert-error" style={{ marginBottom: 20 }}>⚠ {error}</div>}

      {loading && (
        <div className="loading-center">
          <span className="spinner" />
          Loading news…
        </div>
      )}

      {!loading && (
        <div style={{ marginBottom: 20, display: 'flex', alignItems: 'center', gap: 12 }}>
          <div className="stats-badge">
            Page <span>{currentPage}</span> of <span>{pageCount}</span>
          </div>
          <div className="stats-badge">
            <span>{items.length}</span> articles shown
          </div>
        </div>
      )}

      {!loading && items.length === 0 && !error && (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
          <div style={{ fontSize: 40, marginBottom: 16 }}>📭</div>
          <p>No news found. Try adjusting your search.</p>
        </div>
      )}

      <div className="row g-3">
        {items.map(n => (
          <div key={n.id} className="col-12 col-sm-6 col-lg-4 col-xl-3">
            <NewsCard
              news={n}
              onEdit={setEditing}
              onDelete={setDeleting}
            />
          </div>
        ))}
      </div>

      <Pagination current={currentPage} total={pageCount} onChange={handlePage} />

      <NewsModal
        open={addOpen || !!editing}
        editing={editing}
        onClose={() => { setAddOpen(false); setEditing(null); }}
        searchParams={buildApiParams()}
      />
      <DeleteModal
        open={!!deleting}
        news={deleting}
        onClose={() => setDeleting(null)}
        searchParams={buildApiParams()}
      />
    </div>
  );
}
