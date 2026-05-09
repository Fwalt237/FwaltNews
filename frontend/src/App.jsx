import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import store from './store/store';
import Header          from './components/Header';
import Footer          from './components/Footer';
import LoginPage       from './pages/LoginPage';
import SignupPage      from './pages/SignupPage';
import NewsPage        from './pages/NewsPage';
import NewsDetailPage  from './pages/NewsDetailPage';
import AboutPage       from './pages/AboutPage';
import ContactPage     from './pages/ContactPage';
import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css';
import ChatWidget from './components/ChatWidget'; 

function Layout({ children }) {
  return (
    <div className="app-shell">
      <Header />
      <main className="app-main">{children}</main>
      <Footer />
      <ChatWidget /> 
    </div>
  );
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/v1/auth/login"  element={<LoginPage />} />
      <Route path="/v1/auth/signup" element={<SignupPage />} />
      <Route path="/v1/news" element={
          <Layout><NewsPage /></Layout>
      } />
      <Route path="/v1/news/:id" element={
          <Layout><NewsDetailPage /></Layout>
      } />
      <Route path="/about" element={
          <Layout><AboutPage /></Layout>
      } />
      
      <Route path="/contact" element={
          <Layout><ContactPage /></Layout>
      } />
      <Route path="/"  element={<Navigate to="/v1/news" replace />} />
      <Route path="*"  element={<Navigate to="/v1/news" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </Provider>
  );
}
