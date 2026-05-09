import { useState, useEffect } from 'react';
import api from '../services/api';

export function useNewsDetail(id) {
  const [news,     setNews]     = useState(null);
  const [comments, setComments] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);

    Promise.all([
      api.get(`/v1/news/${id}`),
      api.get(`/v1/news/${id}/comments`).catch(() => ({ data: [] })),
    ])
      .then(([newsRes, commentsRes]) => {
        setNews(newsRes.data);
        setComments(Array.isArray(commentsRes.data) ? commentsRes.data : []);
      })
      .catch(e => setError(e.response?.data?.message || 'Failed to load article'))
      .finally(() => setLoading(false));
  }, [id]);

  const postComment = async (content) => {
    try {
      const res = await api.post('/v1/comments', { content, newsId: Number(id) });
      setComments(prev => [res.data, ...prev]); 
      return res.data;
    } catch (e) {
      throw e.response?.data?.message || 'Failed to post comment';
    }
  };

  return { news, comments, loading, error, postComment };
}
