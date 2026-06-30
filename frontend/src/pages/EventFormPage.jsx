import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';

export default function EventFormPage() {
  const { eventId } = useParams();
  const isEdit = Boolean(eventId);
  const navigate = useNavigate();

  const [form, setForm] = useState({ title: '', description: '', capacity: '', location: '', startAt: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    api.get(`/events/${eventId}`)
      .then((event) => setForm({
        title: event.title,
        description: event.description,
        capacity: event.capacity ?? '',
        location: event.location ?? '',
        startAt: event.startAt ? event.startAt.slice(0, 16) : '',
      }))
      .catch((err) => setError(err.message));
  }, [eventId, isEdit]);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    const payload = {
      title: form.title,
      description: form.description,
      capacity: form.capacity === '' ? null : Number(form.capacity),
      location: form.location || null,
      startAt: form.startAt ? `${form.startAt}:00` : null,
    };
    try {
      const saved = isEdit
        ? await api.put(`/admin/events/${eventId}`, payload)
        : await api.post('/admin/events', payload);
      navigate(`/events/${saved.eventId}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>{isEdit ? '행사 수정' : '행사 생성'}</h1>
      <div className="card">
        {error && <div className="error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <label>제목 *</label>
          <input value={form.title} onChange={(e) => update('title', e.target.value)} required />
          <label>설명 *</label>
          <textarea value={form.description} onChange={(e) => update('description', e.target.value)} required />
          <label>정원 (선택)</label>
          <input type="number" min="1" value={form.capacity} onChange={(e) => update('capacity', e.target.value)} />
          <label>장소 (선택)</label>
          <input value={form.location} onChange={(e) => update('location', e.target.value)} />
          <label>일시 (선택)</label>
          <input type="datetime-local" value={form.startAt} onChange={(e) => update('startAt', e.target.value)} />
          <div style={{ marginTop: 16 }} className="row">
            <button type="submit" disabled={loading}>{loading ? '저장 중…' : '저장'}</button>
            <button type="button" className="secondary" onClick={() => navigate(-1)}>취소</button>
          </div>
        </form>
      </div>
    </div>
  );
}
