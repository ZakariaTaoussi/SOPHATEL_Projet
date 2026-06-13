const DEFAULT_API_ORIGIN = 'http://localhost:8081';

export function apiOrigin(): string {
  return DEFAULT_API_ORIGIN;
}

export function apiUrl(path: string): string {
  return `${apiOrigin()}${path}`;
}
