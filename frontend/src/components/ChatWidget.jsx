import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import api from '../services/api';
import '../ChatWidget.css';

const SESSION_ID = (() => {
  let id = sessionStorage.getItem('ai_session');
  if (!id) { id = crypto.randomUUID(); sessionStorage.setItem('ai_session', id); }
  return id;
})();

const SUGGESTIONS = [
  "What's the latest in tech news?",
  "Give me today's top stories",
  "Find articles about climate change",
  "Summarize this week's economy news",
];

function Message({ msg, onArticleClick }) {
  const parts = msg.content.split(/(\[ID:\d+\][^\n]*)/g);

  return (
    <div className={`cw-msg cw-msg--${msg.role}`}>
      {msg.role === 'assistant' && (
        <div className="cw-avatar">🧙‍♂️</div>
      )}
      <div className="cw-bubble">
        {parts.map((part, i) => {
          const match = part.match(/\[ID:(\d+)\] (.*)/);
          if (match) {
            return (
              <button
                key={i}
                className="cw-article-link"
                onClick={() => onArticleClick(Number(match[1]))}
              >
                📰 {match[2]}
              </button>
            );
          }
          return <span key={i}>{part}</span>;
        })}
        <span className="cw-time">
          {new Date(msg.timestamp || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </span>
      </div>
    </div>
  );
}

function ArticlePreview({ article, onClose, onOpen }) {
  if (!article) return null;
  const img = article.imageUrl || article.sourceIcon || '/logo.jpg';
  return (
    <div className="cw-article-preview">
      <button className="cw-preview-close" onClick={onClose}>×</button>
      <img className="cw-preview-img" src={img} alt={article.title}
        onError={e => { e.target.src = '/logo.jpg'; }} />
      <div className="cw-preview-body">
        <div className="cw-preview-tags">
          {article.tagsDto?.slice(0, 3).map(t => (
            <span key={t.id} className="cw-preview-tag">{t.name}</span>
          ))}
        </div>
        <h4 className="cw-preview-title">{article.title}</h4>
        <p className="cw-preview-author">{article.authorDto?.name}</p>
        <button className="cw-preview-open" onClick={() => onOpen(article.id)}>
          Read full article here
        </button>
      </div>
    </div>
  );
}

export default function ChatWidget() {
  const { user }    = useSelector(s => s.auth);
  const navigate    = useNavigate();

  const [open,        setOpen]        = useState(false);
  const [messages,    setMessages]    = useState([]);
  const [input,       setInput]       = useState('');
  const [loading,     setLoading]     = useState(false);
  const [preview,     setPreview]     = useState(null);   
  const [previewData, setPreviewData] = useState(null);   
  const [highlighted, setHighlighted] = useState([]);    

  const bottomRef = useRef(null);
  const inputRef  = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  useEffect(() => {
    if (open) setTimeout(() => inputRef.current?.focus(), 100);
  }, [open]);

  useEffect(() => {
    if (!open || messages.length > 0) return;
    api.get(`/v1/ai/history/${SESSION_ID}`)
      .then(res => {
        if (res.data?.length) {
          setMessages(res.data.map(t => ({
            role: t.role, content: t.content, timestamp: t.timestamp
          })));
        } else {
          setMessages([{
            role: 'assistant',
            content: "Hi! I'm your FwaltNews AI assistant \n\nI can help you:\n• Find articles on any topic\n• Summarize today's news\n• Give you a daily briefing\n\nWhat would you like to know?",
            timestamp: new Date().toISOString(),
          }]);
        }
      })
      .catch(() => {});
  }, [open]);

  const fetchArticlePreview = useCallback(async (id) => {
    setPreview(id);
    try {
      const res = await api.get(`/v1/news/${id}`);
      setPreviewData(res.data);
    } catch {
      setPreviewData(null);
    }
  }, []);

  const send = async (text) => {
    const msg = (text || input).trim();
    if (!msg || loading) return;
    setInput('');

    const userMsg = { role: 'user', content: msg, timestamp: new Date().toISOString() };
    setMessages(prev => [...prev, userMsg]);
    setLoading(true);
    setHighlighted([]);

    try {
      const res = await api.post('/v1/ai/chat', { sessionId: SESSION_ID, message: msg });
      const { message: reply, articleIds } = res.data;

      setMessages(prev => [...prev, {
        role: 'assistant', content: reply, timestamp: new Date().toISOString()
      }]);

      if (articleIds?.length) {
        setHighlighted(articleIds);
        fetchArticlePreview(articleIds[0]);
      }
    } catch (e) {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: 'Sorry, I ran into an error. Please try again.',
        timestamp: new Date().toISOString(),
      }]);
    } finally {
      setLoading(false);
    }
  };

  const clearChat = async () => {
    await api.delete(`/v1/ai/history/${SESSION_ID}`).catch(() => {});
    setMessages([{
      role: 'assistant',
      content: "Chat cleared! How can I help you?",
      timestamp: new Date().toISOString(),
    }]);
    setHighlighted([]);
    setPreview(null);
    setPreviewData(null);
  };

  if (!user) return null; 

  return (
    <>
      {highlighted.length > 0 && (
        <div className="cw-highlighted-strip">
          <span className="cw-strip-label"> I found {highlighted.length} relevant article{highlighted.length > 1 ? 's' : ''}:</span>
          <div className="cw-strip-ids">
            {highlighted.map(id => (
              <button key={id} className="cw-strip-btn" onClick={() => fetchArticlePreview(id)}>
                #{id}
              </button>
            ))}
          </div>
          <button className="cw-strip-view-all" onClick={() => {
            navigate(`/v1/news?aiIds=${highlighted.join(',')}`);
          }}>
            View all here
          </button>
        </div>
      )}

      {preview && (
        <ArticlePreview
          article={previewData}
          onClose={() => { setPreview(null); setPreviewData(null); }}
          onOpen={id => { navigate(`/v1/news/${id}`); setPreview(null); }}
        />
      )}

      <button
        className={`cw-fab ${open ? 'cw-fab--open' : ''}`}
        onClick={() => setOpen(o => !o)}
        title="AI News Assistant"
        aria-label="Open AI assistant"
      >
        {open ? '✕' : '🧙‍♂️'}
      </button>

      {open && (
        <div className="cw-window">
          {}
          <div className="cw-header">
            <div className="cw-header-info">
              <div className="cw-header-avatar">🧙‍♂️</div>
              <div>
                <div className="cw-header-title">FwaltNews AI</div>
                <div className="cw-header-sub">Powered by Gemini 3.1 Flash Lite</div>
              </div>
            </div>
            <button className="cw-header-clear" onClick={clearChat} title="Clear chat">🗑️</button>
          </div>

          <div className="cw-messages">
            {messages.map((m, i) => (
              <Message key={i} msg={m} onArticleClick={fetchArticlePreview} />
            ))}
            {loading && (
              <div className="cw-msg cw-msg--assistant">
                <div className="cw-avatar">🧙‍♂️</div>
                <div className="cw-bubble cw-bubble--typing">
                  <span /><span /><span />
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {messages.length <= 1 && (
            <div className="cw-suggestions">
              {SUGGESTIONS.map(s => (
                <button key={s} className="cw-suggestion" onClick={() => send(s)}>{s}</button>
              ))}
            </div>
          )}

          <div className="cw-input-row">
            <input
              ref={inputRef}
              className="cw-input"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && !e.shiftKey && send()}
              placeholder="Ask about the news…"
              disabled={loading}
            />
            <button
              className="cw-send"
              onClick={() => send()}
              disabled={loading || !input.trim()}
            >
              {loading ? <span className="cw-spinner" /> : '➤'}
            </button>
          </div>
        </div>
      )}
    </>
  );
}
