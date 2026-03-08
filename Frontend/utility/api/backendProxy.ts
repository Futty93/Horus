export function getBackendBaseUrl(): string {
  const ip = process.env.BACKEND_SERVER_IP ?? "localhost";
  const port = process.env.BACKEND_SERVER_PORT ?? "8080";
  return `http://${ip}:${port}`;
}

export async function proxyToBackend(
  path: string,
  init?: RequestInit
): Promise<Response> {
  const base = getBackendBaseUrl();
  const url = `${base}${path}`;
  return fetch(url, init);
}
