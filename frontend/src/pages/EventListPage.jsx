import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

const STATUSES = ['', 'DRAFT', 'OPEN', 'CLOSED', 'COMPLETED', 'CANCELED'];

function formatDate(value) {
  if (!value) return '미정';
  return value.replace('T', ' ').slice(0, 16);
}

export default function EventListPage() {
  const [events, setEvents] = useState([]);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const query = status ? `?status=${status}` : '';
    api.get(`/events${query}`)
      .then(setEvents)
      .catch((err) => setError(err.message));
  }, [status]);

  return (
    <div className="container">
      <div className="between">
        <h1>행사 목록</h1>
        <select value={status} onChange={(e) => setStatus(e.target.value)} style={{ width: 160 }}>
          {STATUSES.map((s) => (
            <option key={s} value={s}>{s === '' ? '전체 상태' : s}</option>
          ))}
        </select>
      </div>
      {error && <div className="error">{error}</div>}
      {events.length === 0 && <p className="muted">표시할 행사가 없습니다.</p>}
      {events.map((event) => (
        <div className="card" key={event.eventId}>
          <div className="between">
            <div>
              <h2>
                <Link to={`/events/${event.eventId}`}>{event.title}</Link>{' '}
                <span className={`badge ${event.status}`}>{event.status}</span>
              </h2>
              <p className="muted">{event.description}</p>
              <p className="muted">
                📍 {event.location || '장소 미정'} · 🗓 {formatDate(event.startAt)}
                {event.capacity ? ` · 정원 ${event.capacity}명` : ''}
              </p>
            </div>
            <Link to={`/events/${event.eventId}`}><button className="secondary small">상세</button></Link>
          </div>
        </div>
      ))}
    </div>
  );
}
