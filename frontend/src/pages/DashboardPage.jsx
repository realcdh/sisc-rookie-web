import { useEffect, useState } from 'react';
import { api } from '../api';

const ITEMS = [
  { key: 'totalEvents', label: '전체 행사' },
  { key: 'openEvents', label: '모집 중 행사' },
  { key: 'totalApplications', label: '전체 신청' },
  { key: 'approvedApplications', label: '승인된 신청' },
  { key: 'attendanceCount', label: '출석' },
  { key: 'totalFeedbacks', label: '피드백' },
];

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/dashboard')
      .then(setData)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div className="container">
      <h1>운영 현황 대시보드</h1>
      {error && <div className="error">{error}</div>}
      {data && (
        <div className="grid">
          {ITEMS.map((item) => (
            <div className="stat" key={item.key}>
              <div className="value">{data[item.key]}</div>
              <div className="label">{item.label}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
