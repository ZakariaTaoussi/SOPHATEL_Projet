const DEFAULT_API_ORIGIN = 'http://localhost:8081';

export function apiOrigin(): string {
  if (typeof window === 'undefined') {
    return DEFAULT_API_ORIGIN;
  }

  return `${window.location.protocol}//${window.location.hostname}:8081`;
}

export function apiUrl(path: string): string {
  return `${apiOrigin()}${path}`;
}
