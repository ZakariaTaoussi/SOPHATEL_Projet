# Gestion Conge

## Configuration locale

Pour lancer le backend en local :

1. Copier `backend/.env.example` vers `backend/.env`.
2. Remplir les vraies valeurs dans `backend/.env`.
3. Lancer Spring Boot depuis le dossier `backend`.

Le fichier `.env` contient des secrets locaux et ne doit jamais etre committe.
Apres exposition d'un secret dans un commit local, regenerer la cle SMTP Brevo,
changer le mot de passe Neon si possible, et changer `JWT_SECRET`.
