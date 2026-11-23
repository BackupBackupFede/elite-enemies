# Elite Enemies

**Rare mobs become elites: stronger, geared, announced — and they drop their gear.**

Every now and then a hostile mob spawns as an elite. It hits harder, takes more to kill, carries
a named piece of gear enchanted beyond vanilla limits, and drops it when it dies.
Nothing to install on the client.

Fabric and NeoForge, Minecraft 1.21.1, 26.2 and 26.3. No dependencies.

## Four ranks

| Rank | Name | Health | Gear examples |
|---|---|---|---|
| Common | ★ grey | ×1.5 | Knockback Stick (Knockback V), Match (Fire Aspect IV) |
| Uncommon | ★★ green | ×1.9 | Apocalypse (Sharpness VI + Smite VI), Multiplier (Piercing + Multishot) |
| Rare | ★★★ purple | ×2.8 | Vorpal Blade (Sharpness VII), Longshot (Power VIII) |
| Legendary | ★★★★ gold | ×3.7 | Kingslayer (Sharpness X), Earthshaker (Density VIII + Breach IV) |

Higher ranks also get more armour, speed, knockback resistance and base damage. The damage bonus
applies to the mob's own attack, not to the weapon it holds.

## What players see

- A **boss bar** with the elite's name and health, within 32 blocks.
- A **chat announcement** for rare and legendary elites, with a sound and a beam of particles.
- Zombies, skeletons, piglins and illagers visibly wear or hold their piece. Every elite drops
  it, undamaged, on top of its normal drops.
- Optional: elites glow through walls in their rank's colour.

## Fair by default

- **No elites from spawners**, spawn eggs or commands: they cannot be farmed.
- **No elites at the bottom of unreachable caves**: an elite only appears within 24 blocks of
  the nearest player's height.
- One elite per minute at most, and never two within 24 blocks in 10 minutes.
- A new elite does not despawn for 5 minutes.
- Elites are always adults (a baby would drop nothing).

## Commands

All for operators.

| Command | |
|---|---|
| `/eliteenemies settings` | the main switches, in a chest menu |
| `/eliteenemies spawn <rank>` | an elite zombie in front of you, to try it out |
| `/eliteenemies reload` | reload the config |

## Config

`config/eliteenemies.json`, written with every default on first start:

| Key | Default | |
|---|---|---|
| `enabled` | `true` | master switch |
| `chancePercent` | `1.0` | chance per eligible mob spawn |
| `cooldownSeconds` | `60` | no other elite anywhere for this long |
| `protectedRadius`, `protectedMinutes` | `24`, `10` | no other elite this close, for this long |
| `persistMinutes` | `5` | a new elite does not despawn |
| `maxHeightGap` | `24` | blocks above or below the nearest player; `0` = no limit |
| `allowSpawners` | `false` | elites from spawners and trial spawners |
| `weightCommon` … `weightLegendary` | `50`, `30`, `9`, `2` | rank weights |
| `announceFrom` | `RARE` | `COMMON` … `LEGENDARY`, or `NONE` |
| `announceRadius` | `128` | chat, sound and beam range |
| `sound`, `beam`, `glow`, `bossBar` | on, on, off, on | |
| `bossBarRange` | `32` | |
| `mobs` | 27 vanilla hostiles | entity ids; modded mobs can be added |

## Good to know

- Elites are plain vanilla mobs with modifiers, a name and entity tags. Remove the mod and they
  stay as named, equipped mobs; nothing else is left in the world.
- An elite is made when it spawns. Mobs already in the world are never converted.

## Credits

Inspired by [Mob Captains](https://modrinth.com/mod/mob-captains) by PuckiSilver (MIT), itself
inspired by Atomic Stryker's Infernal Mobs. Elite Enemies reuses its rank balance and several of
its item names, with thanks. It is a separate mod, not an official port.

## Building

The Gradle process needs JDK 25, even for the 1.21.1 line (its toolchain, Java 21, is downloaded
automatically).

```bash
./gradlew build                             # default line
./gradlew build -Pminecraft_version=1.21.1  # any line of MATRIX in build.gradle
```

Jars land in `fabric/build/libs/` and `neoforge/build/libs/`. `selftest.ps1` runs the in-game
check on every loader and line.

## License

MIT. See [LICENSE](LICENSE).
