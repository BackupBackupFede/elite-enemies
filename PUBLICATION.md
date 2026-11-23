# Publication — fiche à recopier, NON publiée

La description à coller est dans **`DESCRIPTION.md`** : tout le fichier, tel quel (Modrinth et
CurseForge acceptent le Markdown).

## Avant de créer les projets

1. **Envoyer le message à PuckiSilver** (en bas de cette fiche) et lui laisser quelques jours.
2. **Créer le dépôt GitHub** `BackupBackupFede/elite-enemies` : les jars pointent déjà vers
   `https://github.com/BackupBackupFede/elite-enemies` (page) et `.../issues` (bugs).

## Modrinth

| Champ | Valeur |
|---|---|
| Nom | `Elite Enemies` |
| URL (slug) | `elite-enemies` (vérifié libre le 2026-11-10) |
| Type | Mod |
| Résumé | `Rare mobs become elites with named gear enchanted past vanilla limits, a boss bar and a chat announcement. Server-side: works with vanilla clients. Alternative to Mob Captains for 1.21.1 and 26.x.` (196 caractères) |
| Icône | `art\icon-256.png` (136 Ko ; la 512 dépasse la limite de 256 Ko) |
| Catégories | `adventure`, `equipment`, `mobs` |
| Client | **Unsupported** |
| Serveur | **Required** |
| Licence | MIT |
| Source | `https://github.com/BackupBackupFede/elite-enemies` |
| Issues | `https://github.com/BackupBackupFede/elite-enemies/issues` |
| Donation | Ko-fi : `https://ko-fi.com/acesoverdeuces777` |

## CurseForge

| Champ | Valeur |
|---|---|
| Nom | `Elite Enemies` |
| Résumé | `Rare mobs become elites with gear enchanted past vanilla limits. Server-side, vanilla clients.` |
| Icône | `art\icon-512.png` (CurseForge accepte les images plus lourdes) ou `art\icon-256.png` |
| Catégories | Mobs (principale), Adventure and RPG, Server Utility |
| Licence | MIT License |
| Source / Issues | les mêmes liens GitHub |

## Les fichiers, une version par jar

Dans `fabric\build\libs\` et `neoforge\build\libs\`, jamais les `-sources.jar` :

| Jar | Loader | Minecraft |
|---|---|---|
| `eliteenemies-fabric-1.21.1-0.1.0.jar` | Fabric | 1.21.1 |
| `eliteenemies-fabric-26.2-0.1.0.jar` | Fabric | 26.2 |
| `eliteenemies-fabric-26.3-0.1.0.jar` | Fabric | 26.3 |
| `eliteenemies-neoforge-1.21.1-0.1.0.jar` | NeoForge | 1.21.1 |
| `eliteenemies-neoforge-26.2-0.1.0.jar` | NeoForge | 26.2 |
| `eliteenemies-neoforge-26.3-0.1.0.jar` | NeoForge | 26.3 |

Numéro de version : `0.1.0+1.21.1-fabric`, `0.1.0+26.3-neoforge`, etc. (la règle du template).
Canal : **Beta** pour cette première sortie, tant qu'aucun serveur public ne l'a fait tourner.
Dépendances : aucune.

Ou bien : `release.yml` publie tout seul sur les deux sites au push d'un tag `v0.1.0`, une fois
les variables `MODRINTH_ID` / `CURSEFORGE_ID` et les jetons `MODRINTH_TOKEN` / `CURSEFORGE_TOKEN`
configurés dans le dépôt GitHub (voir le README du template). Attention : `mod_version` vaut
`0.1.0` dans `gradle.properties`, le tag doit correspondre.

---

## Message à l'auteur original (à envoyer AVANT la publication)

> Hi PuckiSilver, I'm about to release Elite Enemies, a server-side mod inspired by Mob Captains
> (vanilla clients, Fabric + NeoForge, 1.21.1 and 26.x). It reuses your rank balance and some
> item names under your MIT licence, with credit and a link on the page. Since Mob Captains has
> not been updated since 1.21.10, I wanted to let you know first — if you would rather I change
> anything, or if you plan to update it yourself, just tell me. Thanks for the original!
