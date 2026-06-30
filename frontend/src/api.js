// 백엔드 API 호출 래퍼. 모든 응답은 공통 ApiResponse({ status, message, data }) 형태다.

const TOKEN_KEY = 'sisc_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`/api/v1${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch {
    // 응답 본문이 없을 수 있다.
  }

  if (!response.ok) {
    const message = payload?.message || `요청에 실패했습니다 (HTTP ${response.status})`;
    throw new Error(message);
  }
  // 성공 시 data만 반환한다.
  return payload ? payload.data : null;
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body),
  put: (path, body) => request('PUT', path, body),
  patch: (path, body) => request('PATCH', path, body),
  del: (path, body) => request('DELETE', path, body),
};
