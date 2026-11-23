# Elite Enemies — test client

## Les jars

| Version | Fabric | NeoForge |
|---|---|---|
| **26.3** | `C:\Users\FLO\Projets\MINECRAFT\elite-enemies\fabric\build\libs\eliteenemies-fabric-26.3-0.1.0.jar` | `...\neoforge\build\libs\eliteenemies-neoforge-26.3-0.1.0.jar` |
| 26.2 | `...\fabric\build\libs\eliteenemies-fabric-26.2-0.1.0.jar` | `...\neoforge\build\libs\eliteenemies-neoforge-26.2-0.1.0.jar` |
| **1.21.1** | `...\fabric\build\libs\eliteenemies-fabric-1.21.1-0.1.0.jar` | `...\neoforge\build\libs\eliteenemies-neoforge-1.21.1-0.1.0.jar` |

Après une modification du code : `.\build-all.ps1` dans `elite-enemies`, puis reprendre le jar.

## Quoi tester où

| Combinaison | Pourquoi | Test |
|---|---|---|
| **Fabric 26.3** | la plus jouée | tout |
| **NeoForge 1.21.1** | les gros modpacks ; seule version à noms obfusqués | tout |
| les 4 autres | déjà vertes en test automatique | 1, 2 et 4 seulement |

## Le serveur

Mêmes serveurs que Fair Loot, dans `C:\Users\FLO\Projets\MINECRAFT\_serveurs-test\` :
`fabric-26.3\` (client 26.3 vanilla) et `neoforge-1.21.1\` (client 1.21.1 vanilla).

1. Copier le jar Elite Enemies de la bonne version dans `mods\` du serveur.
2. Double-clic sur `start.bat`, attendre `Done`. Les lignes rouges « Perflib » sont un bruit de
   Windows, sans rapport avec le mod.
3. Fenêtre du serveur : `op <ton pseudo>` (une fois connecté). Si « not white-listed » :
   `whitelist off`.
4. Launcher → la version vanilla correspondante (sans mod) → Multijoueur → Connexion directe →
   `localhost`.
5. Fini : `stop` dans la fenêtre du serveur. Un seul serveur à la fois (même port).

Conseil : `/time set night` et `/gamemode survival` pour les tests de combat ; `/difficulty normal`.

## Tests

- [ ] **1. `/eliteenemies spawn legendary`** : un zombie apparaît devant toi.
      - nom doré « ★★★★ Zombie » quand tu le vises ;
      - message doré « ☠ Legendary elite nearby: Zombie (4 blocks) », en anglais quelle que soit
        la langue du client (le nom du monstre, lui, suit la langue du client) ;
      - son (cri de Wither, aigu) ;
      - colonne de particules dorées au-dessus de lui ;
      - **barre de boss** jaune en haut de l'écran avec son nom, qui baisse quand tu le frappes.
- [ ] **2. Son équipement** : il porte ou tient une pièce visible (épée, armure, arc, masse…).
      En le tuant, elle tombe **intacte**, avec un nom coloré (« Kingslayer », « Aegis »…), des
      étoiles dans la description et des enchantements **au-delà du maximum** (Tranchant IX,
      Protection VIII…) lisibles en chiffres romains. Les drops normaux (chair putréfiée) tombent
      aussi.
- [ ] **3. Les quatre raretés** : `spawn common`, `uncommon`, `rare` → gris ★, vert ★★, violet
      ★★★. Par défaut, seuls rare et légendaire sont annoncés dans le chat ; commun et peu commun
      ont la barre de boss mais pas de message.
- [ ] **4. Apparition naturelle** : `/eliteenemies settings` → « Chance » jusqu'à **10 %**. La
      nuit, en plaine, attendre : un élite apparaît au plus une fois par minute (délai par défaut).
      Remettre 1 % après.
- [ ] **5. Il reste** : un élite ne disparaît pas quand tu t'éloignes à ~60 blocs pendant ses 5
      premières minutes.
- [ ] **6. Spawner** : `/setblock ~ ~ ~2 spawner{SpawnData:{entity:{id:"minecraft:zombie"}}}`,
      chance à 10 % : aucun élite n'en sort. « From spawners » vert : ils peuvent en sortir.
      Remettre rouge.
- [ ] **7. `/eliteenemies settings`** (op) : coffre « Elite Enemies - settings », 9 icônes,
      panneaux verts / rouges / jaunes dessous. Un clic sur l'icône **ou** le panneau change le
      réglage. Shift-clic, touche numérique, `Q` : rien ne bouge, rien ne sort.
- [ ] **8. Les interrupteurs agissent** : « Boss bar » rouge → plus de barre au prochain élite ;
      « Glow » vert → le prochain élite brille à travers les murs, dans sa couleur ; « Beam »
      rouge → plus de colonne ; « Announced in chat » sur « Never » → plus de message.
- [ ] **9. Redémarrer le serveur** : un élite encore vivant garde son nom, son équipement et
      retrouve sa barre de boss quand tu t'approches. Les réglages sont restés.
- [ ] **10. Retirer le jar et redémarrer** : le monde charge ; les élites restants sont de simples
      monstres nommés, toujours équipés.
