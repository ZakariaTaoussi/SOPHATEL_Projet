# Prompt pour Codex - Tests backend Spring Boot

Tu es dans le projet `C:\Users\zakar\Desktop\Stage`, une application Spring Boot + Angular de gestion de conges. Je veux que tu travailles uniquement sur le backend.

Objectif : ajouter une suite de tests backend propre, lisible et maintenable.

Contraintes importantes :

- Ne teste pas les repositories directement.
- Ne modifie pas la logique metier sauf si un test revele un vrai bug bloquant, et dans ce cas explique le bug avant de corriger.
- Ne casse pas les fonctionnalites existantes.
- Garde une architecture de tests simple, clean et comprehensible.
- Utilise les conventions deja presentes dans le projet.
- Ne mets aucun secret reel dans les tests.
- Si des variables d'environnement sont necessaires, utilise des valeurs dummy dans `src/test/resources/application.properties`.
- Les tests doivent etre backend uniquement.

Ce que je veux :

1. Tests unitaires JUnit 5
   - Tester les services importants.
   - Utiliser Mockito pour mocker les dependances : repositories, services externes, `JavaMailSender`, etc.
   - Couvrir les cas succes, erreur metier, validation, permission/statut quand c'est pertinent.
   - Ne pas charger tout le contexte Spring pour un test unitaire simple.

2. Tests API / integration
   - Tester les controllers avec `MockMvc`.
   - Verifier les endpoints importants : auth, forgot/reset password, demandes de conge, absences, profil, dashboard selon ce qui existe dans le backend.
   - Verifier les status HTTP, le JSON retourne, les erreurs attendues, et les regles de securite quand possible.
   - Utiliser `@SpringBootTest` + `@AutoConfigureMockMvc` seulement quand c'est utile.
   - Ne pas tester les repositories directement.

3. Organisation attendue
   - Une structure claire dans `backend/src/test/java`.
   - Des noms de classes explicites, par exemple `PasswordResetServiceImplTest`, `AuthControllerIntegrationTest`, etc.
   - Des fixtures/helpers si cela evite la duplication, mais sans creer une architecture compliquee.
   - Des assertions lisibles.

4. Verification
   - Lance les tests backend avec Maven.
   - Si Maven echoue a cause du reseau ou du sandbox, explique clairement que ce n'est pas une erreur de code.
   - Donne a la fin la liste des fichiers crees/modifies et la commande exacte pour relancer les tests.

Avant de coder :

- Lis d'abord la structure du backend : controllers, services, DTO, security config, exceptions, pom.xml.
- Identifie les zones les plus importantes a couvrir.
- Puis implemente les tests progressivement.

Resultat final attendu :

- Des tests unitaires JUnit/Mockito.
- Des tests API avec MockMvc.
- Aucun test direct des repositories.
- Un backend qui compile et dont les tests passent si l'environnement Maven permet de telecharger les dependances.
