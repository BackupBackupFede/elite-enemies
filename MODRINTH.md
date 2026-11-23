# Page Modrinth — à relire, NON publiée

**Titre :** Elite Enemies

**Résumé (court) :** Rare elite mobs with illegally enchanted gear — server-side, vanilla clients.
Alternative to Mob Captains for 1.21.1 and 26.x.

**Icône :** `art\icon-256.png` (136 Ko). La 512 fait 388 Ko, au-dessus de la limite Modrinth de
256 Ko.

**Déclarations :** `client_side: unsupported`, `server_side: required`. Loaders : Fabric, NeoForge.
Versions : 1.21.1, 26.2, 26.3. Licence : MIT. Catégories : adventure, game-mechanics, mobs.

---

# Elite Enemies

**Rare mobs become elites: stronger, geared, announced — and they drop their gear.**

Every now and then, a zombie, skeleton, spider or blaze spawns as an **elite**. It hits harder,
takes more to kill, wears or holds a named piece of gear **enchanted beyond vanilla limits**, and
drops it when it dies.

**Server-side only.** Players join with a plain vanilla client. One jar, no dependencies, no
Fabric API.

## Four ranks

| Rank | Name | Health | Gear examples |
|---|---|---|---|
| Common | ★ grey | ×1.5 | Knockback Stick (Knockback V), Match (Fire Aspect IV) |
| Uncommon | ★★ green | ×1.9 | Apocalypse (Sharpness VI + Smite VI), Multiplier (Piercing + Multishot) |
| Rare | ★★★ purple | ×2.8 | Vorpal Blade (Sharpness VII), Longshot (Power VIII) |
| Legendary | ★★★★ gold | ×3.7 | Kingslayer (Sharpness X), Earthshaker (Density VIII + Breach IV) |

## What players see

- A **boss bar** with the elite's name and health when they are close to it.
- A **chat announcement** for rare and legendary elites, with a sound and a beam of particles.
- The **gear on the mob**: zombies, skeletons, piglins and illagers visibly wear or hold the
  piece they will drop. Every elite drops its piece, undamaged.

## Fair by default

- **No elites from spawners**: you cannot farm them (one click to allow it).
- **No elites at the bottom of unreachable caves**: an elite only appears close to a player's
  height (adjustable, or off).
- One elite per minute at most, and never two in the same area within 10 minutes.
- A young elite does not despawn for 5 minutes, so you have time to reach it.

## Settings

`/eliteenemies settings` opens a chest menu (operators): on/off, chance, announcements, sound,
beam, boss bar, glow, height limit, spawners. Everything else is in `config/eliteenemies.json`:
rank weights, cooldowns, the list of mobs (modded mobs welcome).

`/eliteenemies spawn <rank>` puts an elite zombie in front of you, to try it out.

## Credits

Inspired by **[Mob Captains](https://modrinth.com/mod/mob-captains)** by PuckiSilver (MIT), itself
inspired by Atomic Stryker's Infernal Mobs. Elite Enemies reuses its rank balance and several of
its item names, with thanks. It is a separate mod, not an official port.

---

## Message à l'auteur original (à envoyer AVANT la publication, avec ton accord)

> Hi PuckiSilver, I'm about to release Elite Enemies, a server-side mod inspired by Mob Captains
> (vanilla clients, Fabric + NeoForge, 1.21.1 and 26.x). It reuses your rank balance and some
> item names under your MIT licence, with credit and a link on the page. Since Mob Captains has
> not been updated since 1.21.10, I wanted to let you know first — if you would rather I change
> anything, or if you plan to update it yourself, just tell me. Thanks for the original!
