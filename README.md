# Manga Afrique+

Appli Android originale. Pas un clone Kitsu.

- targetSdk 35 (Android 15), edge-to-edge, retour predictif
- Explorer : selection editoriale Afrique + recherche manga via l'API publique Kitsu
- Fiche titre et rayons PLAN / READING / DONE, stockes sur l'appareil
- version 0.2.0

Chaque push sur `main` lance GitHub Actions (fichier `.github/workflows/android.yml`).
L'APK de debug est dans l'artifact `manga-afrique-plus-debug` du run.
Sur le telephone : telecharger l'APK, autoriser les sources inconnues, installer.

Internet requis pour la recherche Kitsu.
Les resumes Kitsu restent leur contenu.
