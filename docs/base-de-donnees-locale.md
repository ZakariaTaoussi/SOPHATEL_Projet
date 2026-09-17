# Base de donnees locale

## Pourquoi

Le backend pointait sur une base Neon hebergee (`<votre-endpoint>.aws.neon.tech`).
Depuis le 17/09/2026, le port 5432 sortant est filtre sur ce reseau : la connexion
part mais n'obtient jamais de reponse (`SocketTimeoutException: Connect timed out`).

Consequences observees :

- `mvn spring-boot:run` : Hibernate (`ddl-auto=update`) ne peut pas ouvrir de
  connexion, le contexte Spring echoue, le process sort en code 1 et Maven
  affiche `BUILD FAILURE` — alors que la compilation, elle, reussit.
- `docker compose up backend` : meme erreur, le conteneur sort en code 1.

Verification du filtrage (`portquiz.net` accepte les connexions sur tous les ports) :

| Cible | Resultat |
| --- | --- |
| `portquiz.net:80` | connecte en 55 ms |
| `portquiz.net:5432` | timeout |
| hote Neon `:443` | connecte en 41 ms |
| hote Neon `:5432` | timeout |

Neon n'expose le protocole Postgres que sur le port 5432 ; son port 443 est
reserve au proxy WebSocket et refuse la negociation TLS Postgres
(`no_application_protocol`). Il n'y a donc pas de contournement par le port.

La base est donc desormais un Postgres local, joignable en loopback / reseau
Docker, deux chemins qu'aucun filtrage sortant ne traverse.

## Demarrage

```bash
# Postgres + NATS
docker compose up -d --wait postgres nats

# soit le backend hors Docker (utilise backend/.env -> localhost:5432)
cd backend && ./mvnw spring-boot:run

# soit toute la stack
docker compose up -d --build --wait backend
```

Le schema est cree par Hibernate (`spring.jpa.hibernate.ddl-auto=update`) au
premier demarrage, et `DataInitializer` cree les comptes de base
(`admin@sophatel.com` / `admin123`, `employe|rh|responsable|dg@sophatel.com` /
`password123`).

## Donnees

Les donnees de production Neon (477 lignes) ont ete exportees via l'API
SQL-sur-HTTPS de Neon, qui passe par le port 443 et reste donc accessible :

```bash
cd backend
python scripts/neon-export.py            # -> db-local/neon-data.sql
```

Rechargement dans la base locale (le backend doit avoir demarre une premiere
fois pour que les tables existent) :

```bash
docker exec -i sophatel-postgres \
  psql -U sophatel -d DB_Sophatel -v ON_ERROR_STOP=1 < backend/db-local/neon-data.sql
```

Le fichier genere vide chaque table puis reinsere les lignes, contraintes FK
desactivees le temps du chargement (`session_replication_role = replica`), et
recale les sequences IDENTITY. Il est rejouable autant de fois que necessaire.

`backend/db-local/` est dans `.gitignore` : l'export contient des emails reels
et des hash de mots de passe.

## Comptes de connexion

| Compte | Mot de passe |
| --- | --- |
| `admin@sophatel.com` | `admin123` |
| `<votre-compte-personnel>` | `password123` |
| `employe@sophatel.com` | `password123` |
| `rh@sophatel.com` | `password123` |
| `responsable@sophatel.com` | `password123` |
| `dg@sophatel.com` | `password123` |

Dans les donnees Neon, quatre comptes (`<votre-compte-personnel>`,
`employe@`, `rh@`, `dg@`) partageaient un hash bcrypt **identique**, visiblement
copie-colle d'une ligne a l'autre, dont le mot de passe d'origine est inconnu :
aucun des trois candidats evidents (`password123`, `admin123`, prenom+annee) ne
correspondait. Ces quatre comptes etaient donc inutilisables.

Ils ont ete realignes sur le hash de `responsable@sophatel.com`, donc sur
`password123` :

```bash
docker exec sophatel-postgres psql -U sophatel -d DB_Sophatel -c \
  "update utilisateurs set password = (select password from utilisateurs where email='responsable@sophatel.com') \
   where email in ('<votre-compte-personnel>','employe@sophatel.com','rh@sophatel.com','dg@sophatel.com')"
```

Attention : recharger `db-local/neon-data.sql` remet les anciens hash
inutilisables. Rejouer la commande ci-dessus apres chaque rechargement.

A noter aussi : cote Neon, la ligne `<votre-compte-personnel>` contient
desormais un mot de passe **en clair** (11 caracteres) et non un hash bcrypt.
Un tel format ne peut jamais authentifier :
`BCryptPasswordEncoder.matches()` renvoie false sur une valeur qui n'est pas un
hash. Si la base Neon est reutilisee un jour, ce mot de passe doit etre
re-encode via le flux "Mot de passe oublie" de l'application.

## Revenir sur Neon

Le jour ou le port 5432 sera ouvert (autre reseau, VPN), recopier dans
`backend/.env` les valeurs `NEON_DB_URL` / `NEON_DB_USERNAME` /
`NEON_DB_PASSWORD` vers `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`, et retirer la
surcharge `DB_*` du service `backend` dans `docker-compose.yml`.

Test rapide du port depuis PowerShell :

```powershell
Test-NetConnection -ComputerName <votre-endpoint>.eu-central-1.aws.neon.tech -Port 5432
```
