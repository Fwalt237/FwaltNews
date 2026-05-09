import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useNewsDetail } from '../hooks/useNewsDetail';

const LOGO     = '/logo.jpg';
const fmtDate  = s => s ? new Date(s).toLocaleDateString('en-US',
  { month: 'long', day: 'numeric', year: 'numeric' }) : '';
const fmtTime  = s => s ? new Date(s).toLocaleTimeString('en-US',
  { hour: '2-digit', minute: '2-digit' }) : '';
const stripHtml = s => s?.replace(/<[^>]*>/g, '') ?? '';

function CommentBubble({ comment }) {
  const author = comment.authorName || 'Anonymous';
  return (
    <div className="detail-comment">
      <div className="detail-comment-avatar">
        {author[0].toUpperCase()}
      </div>
      <div className="detail-comment-body">
        <div className="detail-comment-meta">
          <span className="detail-comment-author">{author}</span>
          <span className="detail-comment-date">{fmtDate(comment.createdDate)}</span>
        </div>
        <p className="detail-comment-text">{comment.content}</p>
      </div>
    </div>
  );
}

export default function NewsDetailPage() {
  const { id }     = useParams();
  const navigate   = useNavigate();
  const { user }   = useSelector(s => s.auth);
  const isLoggedIn = !!user;

  const { news, comments, loading, error, postComment } = useNewsDetail(id);

  const [commentText, setCommentText] = useState('');
  const [posting,     setPosting]     = useState(false);
  const [commentErr,  setCommentErr]  = useState('');

  const imgSrc = news?.imageUrl || news?.sourceIcon || LOGO;

const bodyText = stripHtml(news?.content);

  const submitComment = async e => {
    e.preventDefault();
    if (!commentText.trim()) return;
    setPosting(true);
    setCommentErr('');
    try {
      await postComment(commentText.trim());
      setCommentText('');
    } catch (err) {
      setCommentErr(err);
    } finally {
      setPosting(false);
    }
  };

  if (loading) return (
    <div className="detail-shell">
      <div className="loading-center" style={{ minHeight: '60vh' }}>
        <span className="spinner" />
        Loading article…
      </div>
    </div>
  );

  if (error || !news) return (
    <div className="detail-shell">
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <div style={{ fontSize: 48, marginBottom: 16 }}>📭</div>
        <p style={{ color: 'var(--text-muted)', marginBottom: 24 }}>{error || 'Article not found'}</p>
        <button className="btn-ghost" onClick={() => navigate('/news')}>Back to News</button>
      </div>
    </div>
  );

  return (
    <div className="detail-shell">

      <article className="detail-article">

        <header className="detail-header">
          {news.tagsDto?.length > 0 && (
            <div className="detail-tags">
              {news.tagsDto.map(t => (
                <span key={t.id} className="tag-badge">{t.name}</span>
              ))}
            </div>
          )}

          <h1 className="detail-title">{news.title}</h1>

          <div className="detail-byline">
            <div className="detail-author-chip">
              <div className="detail-author-avatar">
                {(news.authorDto?.name || 'A')[0].toUpperCase()}
              </div>
              <div>
                <div className="detail-author-name">
                  {news.authorDto?.name || 'Unknown Author'}
                </div>
                <div className="detail-author-date">
                  Updated {fmtDate(news.lastUpdatedDate)} at {fmtTime(news.lastUpdatedDate)}
                </div>
              </div>
            </div>
          </div>
        </header>

        <div className="detail-img-wrap">
          <img
            className="detail-img"
            src={imgSrc}
            alt={news.title}
            onError={e => { e.target.onerror = null; e.target.src = LOGO; }}
          />
        </div>

        <div className="detail-body">
          <p className="detail-content">{bodyText}</p>
        </div>

      </article>

      <section className="detail-comments-section">
        <h2 className="detail-comments-title">
          Comments <span className="detail-comments-count">{comments.length}</span>
        </h2>

        {isLoggedIn ? (
          <form className="detail-comment-form" onSubmit={submitComment}>
            <div className="detail-comment-form-inner">
              <div className="detail-author-avatar" style={{ flexShrink: 0 }}>
                {user.username[0].toUpperCase()}
              </div>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 10 }}>
                <textarea
                  className="field-input"
                  rows={3}
                  value={commentText}
                  onChange={e => setCommentText(e.target.value)}
                  placeholder="Share your thoughts on this article…"
                />
                {commentErr && <div className="alert-error">⚠ {commentErr}</div>}
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button
                    type="submit"
                    className="btn-red sm"
                    disabled={posting || !commentText.trim()}
                  >
                    {posting
                      ? <><span className="spinner" style={{ width: 12, height: 12 }} /> Posting…</>
                      : 'Post Comment'}
                  </button>
                </div>
              </div>
            </div>
          </form>
        ) : (
          <div className="detail-login-prompt">
            <span>Sign in to leave a comment</span>
            <button className="btn-red sm" onClick={() => navigate('/v1/auth/login')}>Sign In</button>
          </div>
        )}

        <div className="detail-comment-list">
          {comments.length === 0
            ? <p className="detail-no-comments">No comments yet. Be the first!</p>
            : comments.map(c => <CommentBubble key={c.id} comment={c} />)
          }
        </div>
      </section>
    </div>
  );
}
