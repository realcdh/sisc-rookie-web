import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { useAuth, isStaff, isAdmin } from '../auth';

const EVENT_STATUSES = ['DRAFT', 'OPEN', 'CLOSED', 'COMPLETED', 'CANCELED'];

function formatDate(value) {
  if (!value) return '미정';
  return value.replace('T', ' ').slice(0, 16);
}

export default function EventDetailPage() {
  const { eventId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const staff = isStaff(user);
  const admin = isAdmin(user);

  const [event, setEvent] = useState(null);
  const [myApp, setMyApp] = useState(null);
  const [apps, setApps] = useState([]);
  const [attendances, setAttendances] = useState([]);
  const [feedbacks, setFeedbacks] = useState([]);
  const [code, setCode] = useState('');
  const [feedbackText, setFeedbackText] = useState('');
  const [issuedCode, setIssuedCode] = useState(null);
  const [nextStatus, setNextStatus] = useState('OPEN');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const loadEvent = useCallback(() => {
    api.get(`/events/${eventId}`).then(setEvent).catch((e) => setError(e.message));
  }, [eventId]);

  const loadMyApp = useCallback(() => {
    // 내 신청이 없으면 404가 나므로 조용히 null 처리한다.
    api.get(`/events/${eventId}/applications/me`).then(setMyApp).catch(() => setMyApp(null));
  }, [eventId]);

  const loadStaffData = useCallback(() => {
    if (!staff) return;
    api.get(`/events/${eventId}/applications`).then(setApps).catch(() => {});
    api.get(`/events/${eventId}/attendances`).then(setAttendances).catch(() => {});
    api.get(`/events/${eventId}/feedbacks`).then(setFeedbacks).catch(() => {});
  }, [eventId, staff]);

  useEffect(() => {
    loadEvent();
    loadMyApp();
    loadStaffData();
  }, [loadEvent, loadMyApp, loadStaffData]);

  async function run(fn, successMsg) {
    setError('');
    setNotice('');
    try {
      await fn();
      if (successMsg) setNotice(successMsg);
    } catch (e) {
      setError(e.message);
    }
  }

  const apply = () => run(async () => {
    await api.post(`/events/${eventId}/applications`);
    loadMyApp();
    loadStaffData();
  }, '행사 신청이 완료되었습니다.');

  const cancel = () => run(async () => {
    await api.del(`/events/${eventId}/applications/me`);
    loadMyApp();
    loadStaffData();
  }, '신청이 취소되었습니다.');

  const checkIn = () => run(async () => {
    await api.post(`/events/${eventId}/attendances`, { code });
    setCode('');
    loadStaffData();
  }, '출석이 완료되었습니다.');

  const submitFeedback = () => run(async () => {
    await api.post(`/events/${eventId}/feedbacks`, { content: feedbackText });
    setFeedbackText('');
    loadStaffData();
  }, '피드백이 등록되었습니다.');

  const changeStatus = () => run(async () => {
    await api.patch(`/admin/events/${eventId}/status`, { status: nextStatus });
    loadEvent();
  }, '행사 상태가 변경되었습니다.');

  const removeEvent = () => run(async () => {
    await api.del(`/admin/events/${eventId}`);
    navigate('/');
  });

  const review = (applicationId, status) => run(async () => {
    await api.patch(`/events/${eventId}/applications/${applicationId}/status`, { status });
    loadStaffData();
  }, '신청 상태가 변경되었습니다.');

  const issueCode = () => run(async () => {
    const result = await api.post(`/admin/events/${eventId}/attendance-codes`, {});
    setIssuedCode(result.code);
  }, '출석 코드가 발급되었습니다.');

  if (!event) {
    return <div className="container">{error ? <div className="error">{error}</div> : <p className="muted">불러오는 중…</p>}</div>;
  }

  return (
    <div className="container">
      {error && <div className="error">{error}</div>}
      {notice && <div className="notice">{notice}</div>}

      {/* 행사 정보 */}
      <div className="card">
        <div className="between">
          <h1>{event.title} <span className={`badge ${event.status}`}>{event.status}</span></h1>
          {admin && <Link to={`/admin/events/${eventId}/edit`}><button className="secondary small">수정</button></Link>}
        </div>
        <p>{event.description}</p>
        <p className="muted">
          📍 {event.location || '장소 미정'} · 🗓 {formatDate(event.startAt)}
          {event.capacity ? ` · 정원 ${event.capacity}명` : ''} · 주최 {event.createdByName}
        </p>
      </div>

      {/* 관리자: 상태 변경 / 삭제 */}
      {admin && (
        <div className="card">
          <h2>행사 관리 (관리자)</h2>
          <div className="row">
            <select value={nextStatus} onChange={(e) => setNextStatus(e.target.value)} style={{ width: 180 }}>
              {EVENT_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
            <button onClick={changeStatus}>상태 변경</button>
            <button className="danger" onClick={removeEvent}>행사 삭제</button>
          </div>
        </div>
      )}

      {/* 내 신청 / 신청하기 */}
      <div className="card">
        <h2>내 신청</h2>
        {myApp ? (
          <div className="row">
            <span>상태: <span className={`badge ${myApp.status}`}>{myApp.status}</span></span>
            {myApp.attended && <span className="badge COMPLETED">출석완료</span>}
            {myApp.status === 'PENDING' && <button className="secondary small" onClick={cancel}>신청 취소</button>}
          </div>
        ) : (
          <div className="row">
            <span className="muted">아직 신청하지 않았습니다.</span>
            <button onClick={apply} disabled={event.status !== 'OPEN'}>신청하기</button>
            {event.status !== 'OPEN' && <span className="muted">(모집 중인 행사만 신청 가능)</span>}
          </div>
        )}
      </div>

      {/* 출석 체크 */}
      <div className="card">
        <h2>출석 체크</h2>
        <div className="row">
          <input
            placeholder="출석 코드 입력"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            style={{ width: 200 }}
          />
          <button onClick={checkIn} disabled={!code}>출석하기</button>
          <span className="muted">승인된 신청자만 출석할 수 있습니다.</span>
        </div>
      </div>

      {/* 피드백 작성 */}
      <div className="card">
        <h2>피드백 작성</h2>
        <textarea
          placeholder="행사에 대한 피드백을 남겨주세요."
          value={feedbackText}
          onChange={(e) => setFeedbackText(e.target.value)}
        />
        <div style={{ marginTop: 8 }}>
          <button onClick={submitFeedback} disabled={!feedbackText.trim()}>피드백 등록</button>
        </div>
      </div>

      {/* 운영진: 신청자 목록 */}
      {staff && (
        <div className="card">
          <h2>신청자 목록 (운영진)</h2>
          {apps.length === 0 ? <p className="muted">신청자가 없습니다.</p> : (
            <table>
              <thead>
                <tr><th>신청자</th><th>상태</th><th>신청일</th><th>처리</th></tr>
              </thead>
              <tbody>
                {apps.map((a) => (
                  <tr key={a.applicationId}>
                    <td>{a.memberName}</td>
                    <td><span className={`badge ${a.status}`}>{a.status}</span></td>
                    <td className="muted">{formatDate(a.appliedAt)}</td>
                    <td>
                      {a.status === 'PENDING' ? (
                        <div className="row">
                          <button className="small" onClick={() => review(a.applicationId, 'APPROVED')}>승인</button>
                          <button className="small danger" onClick={() => review(a.applicationId, 'REJECTED')}>반려</button>
                        </div>
                      ) : <span className="muted">—</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* 운영진: 출석 코드 발급 / 현황 */}
      {staff && (
        <div className="card">
          <h2>출석 관리 (운영진)</h2>
          <div className="row">
            <button onClick={issueCode}>출석 코드 발급</button>
            {issuedCode && <span>발급된 코드: <strong style={{ fontSize: 18 }}>{issuedCode}</strong></span>}
          </div>
          <h3>출석 현황 ({attendances.length}명)</h3>
          {attendances.length === 0 ? <p className="muted">출석자가 없습니다.</p> : (
            <table>
              <thead><tr><th>이름</th><th>출석 시각</th></tr></thead>
              <tbody>
                {attendances.map((at) => (
                  <tr key={at.attendanceId}><td>{at.memberName}</td><td className="muted">{formatDate(at.checkedInAt)}</td></tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* 운영진: 피드백 목록 */}
      {staff && (
        <div className="card">
          <h2>피드백 목록 (운영진)</h2>
          {feedbacks.length === 0 ? <p className="muted">피드백이 없습니다.</p> : (
            <ul>
              {feedbacks.map((f) => (
                <li key={f.feedbackId} style={{ marginBottom: 8 }}>
                  <strong>{f.memberName}</strong> · <span className="muted">{formatDate(f.createdAt)}</span>
                  <div>{f.content}</div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
