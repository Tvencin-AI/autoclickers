# ⚡ AutoClicker

Auto-clicker personnel pour Android. Aucun tracking, aucun réseau, code 100% ouvert.

## Fonctionnalités
- Cibles draggables repositionnables sur l'écran
- Multi-cibles avec rotation séquentielle
- Intervalle configurable en millisecondes
- Bouton ▶/⏸ flottant et discret sur le bord de l'écran
- Raccourcis directs vers les permissions système

## Build via GitHub Actions

1. Crée un repo GitHub (ex: `autoclicker`)
2. Push ce projet dedans
3. Va dans **Actions** → le build se lance automatiquement
4. Télécharge l'APK dans **Artifacts**

## Installation sur Android

1. Dans les paramètres Android : activer **Sources inconnues** (ou "Installer des apps inconnues")
2. Installer l'APK
3. Ouvrir l'app et suivre les étapes à l'écran :
   - Étape 1a : Autoriser l'affichage par-dessus les autres apps
   - Étape 1b : Activer le service d'accessibilité "AutoClicker"
   - Étape 2 : Configurer l'intervalle et le nombre de cibles
   - Appuyer sur **Lancer les cibles flottantes**

## Utilisation
- Les cibles (cercles verts) apparaissent sur l'écran — fais-les glisser où tu veux
- Le bouton ▶/⏸ est sur le bord droit — glisse-le verticalement pour le repositionner
- Appuie sur ▶ pour démarrer les clics automatiques
- Les clics tournent entre toutes les cibles dans l'ordre
