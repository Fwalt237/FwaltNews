import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';

const LOGO = '/logo.jpg';
const fmtDate = s =>
  s ? new Date(s).toLocaleDateString('en-US', { 
      month: 'long', 
      day: 'numeric', 
      year: 'numeric' 
    }) : '';

const stripHtml = s => s?.replace(/<[^>]*>/g, '') ?? '';

export default function NewsCard({ news, onEdit, onDelete }) {
  const navigate = useNavigate();
  const { user } = useSelector(s => s.auth);
  const isAdmin  = user?.roles?.includes('ROLE_ADMIN');

  const imgSrc = news.imageUrl || news.sourceIcon || LOGO;
  const plain  = stripHtml(news.content);

  const goToDetail = e => {
    if (e.target.closest('.card-action-btn')) return;
    navigate(`/v1/news/${news.id}`);
  };

  return (
    <article
      className="nc-card"
      onClick={goToDetail}
      role="button"
      tabIndex={0}
      onKeyDown={e => e.key === 'Enter' && goToDetail(e)}
    >
      <div className="nc-img-wrap">
        <img
          className="nc-img"
          src={imgSrc}
          alt={news.title}
          onError={e => { e.target.onerror = null; e.target.src = LOGO; }}
        />
        {news.tagsDto?.length > 0 && (
          <div className="nc-tags-overlay">
            {news.tagsDto.slice(0, 2).map(t => (
              <span key={t.id} className="nc-tag">{t.name}</span>
            ))}
            {news.tagsDto.length > 2 && (
              <span className="nc-tag nc-tag-more">+{news.tagsDto.length - 2}</span>
            )}
          </div>
        )}
      </div>

      <div className="nc-body">
        <div className="nc-meta">
          {news.authorDto?.name && <span className="nc-author">{news.authorDto.name}</span>}
          <span className="nc-dot">·</span>
          <span className="nc-date">{fmtDate(news.createdDate)}</span>
        </div>
        <h3 className="nc-title">{news.title}</h3>
        <p className="nc-excerpt">
          {plain.length > 120 ? plain.slice(0, 120).trimEnd() + '…' : plain}
        </p>
        <span className="nc-read-more">Read more</span>
      </div>

      {isAdmin && (
        <div className="nc-actions">
          <button
            className="card-action-btn nc-btn-edit"
            title="Edit"
            onClick={e => { e.stopPropagation(); onEdit(news); }}
          >✏️</button>
          <button
            className="card-action-btn nc-btn-del"
            title="Delete"
            onClick={e => { e.stopPropagation(); onDelete(news); }}
          >🗑️</button>
        </div>
      )}
    </article>
  );
}
