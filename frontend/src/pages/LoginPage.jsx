import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@sisc.test');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await api.post('/auth/login', { email, password });
      login(data);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-wrap">
      <div className="card">
        <h1>로그인</h1>
        {error && <div className="error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <label>이메일</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          <label>비밀번호</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          <div style={{ marginTop: 16 }}>
            <button type="submit" disabled={loading}>{loading ? '로그인 중…' : '로그인'}</button>
          </div>
        </form>
        <div className="demo-accounts">
          <strong>시연 계정</strong> (비밀번호 <code>password123</code>)<br />
          관리자 <code>admin@sisc.test</code><br />
          운영진 <code>staff@sisc.test</code><br />
          부원 <code>member@sisc.test</code>
        </div>
      </div>
    </div>
  );
}
