# Compte rendu TP Taquin SMA

> <u>Auteurs</u> : **Nathan Peyronnet** et **Émilie Vey**  
> <u>Date</u> : 12/06/2023

### Taille maximale du taquin

Notre code a réussi à résoudre le taquin 3x3 (8 pièces) en une dizaine de minutes. Nous pensons qu'il est capable de faire le 5x5 (24 pièces).

### Architecture

Notre application se découpe ainsi :

- [`TaquinSmaApplication`](src/main/java/com/polypote/taquinsma/TaquinSmaApplication.java), classe principale qui lance l'application, génère la grille, et gère l'affichage.
- [`Agent`](src/main/java/com/polypote/taquinsma/game/Agent.java), classe qui représente un agent, pouvant trouver un chemin vers sa case cible, et envoyer des messages aux autres agents.
- [`Case`](src/main/java/com/polypote/taquinsma/game/Case.java), classe qui représente une case de la grille, avec sa position et son potentiel agent.
- [`Message`](src/main/java/com/polypote/taquinsma/game/Message.java), classe qui représente un message envoyé par un agent à un autre.
- [`MessageType`](src/main/java/com/polypote/taquinsma/game/MessageType.java), énumération des différents types de messages (Demande de se déplacer ou Fin de déplacement).

![Diagramme de classes](<src/main/resources/class Diagram.png>)

### Fonctionnalités

- Génération d'une grille aléatoire
  - Placement aléatoire des agents (en évitant les superpositions)
  - Placement aléatoire de leur case cible (en évitant les superpositions)
- Affichage de la grille
- Algorithmes de recherche de chemin
  - Dijkstra
  - Distance euclidienne
- Déplacement des agents
- Envoi de messages entre agents
- Résolution du taquin

### Essais

L'algorithme de Dijkstra donne des résultats questionnables. En effet, le chemin trouvé vers la case objectif comporte régulièrement des déplacements impossibles : mouvements en diagonal ou téléportation.

