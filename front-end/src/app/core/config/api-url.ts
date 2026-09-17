const API_PORT = 8081;

// Utilise uniquement pendant le rendu serveur (SSR), ou `window` n'existe pas.
const FALLBACK_API_ORIGIN = `http://localhost:${API_PORT}`;

export function apiOrigin(): string {
  // Le navigateur a charge la page depuis la machine qui heberge l'application :
  // le backend est sur ce meme hote, port 8081. Deduire l'origine plutot que de
  // l'ecrire en dur evite de recompiler le front pour chaque environnement --
  // localhost en developpement, l'IP de la VM en deploiement.
  if (typeof window !== 'undefined' && window.location?.hostname) {
    return `${window.location.protocol}//${window.location.hostname}:${API_PORT}`;
  }

  return FALLBACK_API_ORIGIN;
}

export function apiUrl(path: string): string {
  return `${apiOrigin()}${path}`;
}
