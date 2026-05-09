import { configureStore, createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../services/api';
import { jwtDecode } from 'jwt-decode';

const storedUser  = (() => { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } })();
const storedToken = localStorage.getItem('token');

export const login = createAsyncThunk('auth/login', async (creds, { rejectWithValue }) => {
  try {
    const res = await api.post('v1/auth/login', creds);
    return res.data;
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Invalid credentials');
  }
});

export const signup = createAsyncThunk('auth/signup', async (data, { rejectWithValue }) => {
  try {
    const res = await api.post('v1/auth/signup', data);
    return res.data;
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Registration failed');
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: { user: storedUser, token: storedToken, loading: false, error: null },
  reducers: {
    loginSuccess(state, action) {
      const { token } = action.payload;
      state.token = token;
      
      localStorage.setItem('token', token);

      try {
        const decoded = jwtDecode(token);

        state.user = {
          username: decoded.sub,
          roles: decoded.roles || []
        };
        
        localStorage.setItem('user', JSON.stringify(state.user));
      } catch (e) {
        console.error("Token decoding failed:", e);
      }
    },
    logout(state) {
      state.user = null;
      state.token = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    },
    clearAuthError(state) { state.error = null; },
  },
  extraReducers: b => {
    const handlePending  = s => { s.loading = true;  s.error = null; };
    const handleFulfilled = (s, a) => {
      s.loading = false;
      s.token = a.payload.token;
      s.user  = { username: a.payload.username, email: a.payload.email, roles: a.payload.roles };
      localStorage.setItem('token', a.payload.token);
      localStorage.setItem('user', JSON.stringify(s.user));
    };
    const handleRejected = (s, a) => { s.loading = false; s.error = a.payload; };
    b.addCase(login.pending,   handlePending)
     .addCase(login.fulfilled, handleFulfilled)
     .addCase(login.rejected,  handleRejected)
     .addCase(signup.pending,   handlePending)
     .addCase(signup.fulfilled, handleFulfilled)
     .addCase(signup.rejected,  handleRejected);
  },
});
export const { logout, clearAuthError, loginSuccess } = authSlice.actions;

export const fetchNews = createAsyncThunk('news/fetchAll', async (params, { rejectWithValue }) => {
  try {
    const res = await api.get('/v1/news', { params });
    return res.data;
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Failed to fetch news');
  }
});

export const readNews = createAsyncThunk( 'news/read', async (id, { rejectWithValue }) => {     
    try {
      const res = await api.get(`/v1/news/${id}`);
      return res.data;                           
    } catch (e) {
      return rejectWithValue(e.response?.data?.message || 'Failed to load news');
    }
  }
);

export const createNews = createAsyncThunk('news/create', async (data, { rejectWithValue }) => {
  try {
    const res = await api.post('/v1/news', data);
    return res.data;
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Failed to create news');
  }
});

export const updateNews = createAsyncThunk('news/update', async ({ id, data }, { rejectWithValue }) => {
  try {
    const res = await api.patch(`/v1/news/${id}`, data);
    return res.data;
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Failed to update news');
  }
});

export const deleteNews = createAsyncThunk('news/delete', async (id, { rejectWithValue }) => {
  try {
    await api.delete(`/v1/news/${id}`);
    return id;
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Failed to delete news');
  }
});

const newsSlice = createSlice({
    name: 'news',
    initialState: {
        items: [],          
        currentNews: null,  
        total: 0,            
        pageCount: 0,        
        currentPage: 1,      
        loading: false,      
        error: null          
    },
    reducers: {
        clearNewsError(state) { state.error = null; },
        clearCurrentNews(state) { state.currentNews = null; } 
    },
    extraReducers: (builder) => {
        const handlePending = (state) => { state.loading = true; state.error = null; };
        const handleRejected = (state, action) => { state.loading = false; state.error = action.payload; };

        builder
            .addCase(fetchNews.pending, handlePending)
            .addCase(fetchNews.fulfilled, (state, action) => {
                state.loading = false;
                state.items = action.payload.modelDtoList ?? [];
                state.total = action.payload.totalCount ?? 0;
                state.pageCount = action.payload.pageCount ?? 1;
                state.currentPage = action.payload.currentPage ?? 1;
            })
            .addCase(fetchNews.rejected, handleRejected)

            .addCase(readNews.pending, handlePending)
            .addCase(readNews.fulfilled, (state, action) => {
                state.loading = false;
                state.currentNews = action.payload;
            })
            .addCase(readNews.rejected, handleRejected)

            .addCase(createNews.pending, handlePending)
            .addCase(createNews.fulfilled, (state) => {
                state.loading = false;
            })
            .addCase(createNews.rejected, handleRejected)

            .addCase(updateNews.pending, handlePending)
            .addCase(updateNews.fulfilled, (state) => {
                state.loading = false;
                state.currentNews = null;
            })
            .addCase(updateNews.rejected, handleRejected)

            .addCase(deleteNews.pending, handlePending)
            .addCase(deleteNews.fulfilled, (state, action) => {
                state.loading = false;
                state.items = state.items.filter(item => item.id !== action.payload);
            })
            .addCase(deleteNews.rejected, handleRejected);
    },
});

export const { clearNewsError, clearCurrentNews } = newsSlice.actions;

const store = configureStore({
  reducer: { auth: authSlice.reducer, news: newsSlice.reducer },
});
export default store;
