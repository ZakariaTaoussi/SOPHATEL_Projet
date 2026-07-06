# Jenkins local avec Docker

Cette configuration lance Jenkins avec le Docker CLI installe dans le conteneur.
Elle permet au pipeline de lancer `docker build` sur les images backend et frontend.

## Demarrer Jenkins

Depuis la racine du projet :

```powershell
docker rm jenkins
docker compose -f jenkins/docker-compose.yml up -d --build
```

Si le conteneur `jenkins` n'existe pas, la commande `docker rm jenkins` peut afficher une erreur. Ce n'est pas grave.

## Verifier Docker dans Jenkins

```powershell
docker exec -it jenkins docker --version
docker exec -it jenkins docker ps
```

Si ces commandes fonctionnent, l'etape `Docker build` du `Jenkinsfile` peut utiliser Docker.

## Configuration Jenkins a garder

Dans Jenkins, verifier :

```text
Administrer Jenkins > Tools > NodeJS installations
Nom : nodejs-20
Version : NodeJS 20.19.x ou plus recent
```

Le nom doit correspondre au bloc `tools` du `Jenkinsfile`.
