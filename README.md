Weapons Effect (NeoForge 26.2)
=======

Weapons with effects and abilities, made with the ores of **Simple Ores**.

- Minecraft **26.2**, NeoForge **26.2.0.88**, Java **25**
- **Requires Simple Ores** (26.2, version `2.0.0-26.2` or newer)
- Optional: **JEI** 26.2

Crafting: the Gravity Core
==========

The old *Simple Crafting Table* is gone. The weapons are now crafted in the
**Gravity Core** of Simple Ores: place a core, throw the ingredients into it and
they start orbiting; when all the ingredients of a weapon are there, the core
collapses them into the weapon.

The recipes are in `src/main/resources/data/weaponseffect/recipe/gravity_core/`
(type `simpleores:gravity_craft`, see `GravityCoreRecipe` in Simple Ores for every option).

| Weapon | Core (minimum tier) | Ingredients |
|---|---|---|
| Blindness Dagger | Sun (1) | 2 Nexalite, 1 Stick |
| Swiftness Dagger | Sun (1) | 2 Astralite, 1 Stick |
| Poison Sword | Sun (1) | 3 Nexalite, 4 Sticks |
| Ice Sword | Sun (1) | 3 Astralite, 4 Sticks |
| Lance | Sun (1) | 5 Nexalite, 4 Sticks |
| Wither Sword | Sun (1) | 3 Nexalite Blocks, 4 Sticks |
| Fire Sword | Sun (1) | 3 Ignithra Blocks, 4 Blaze Rods |
| Gimli's Axe | Red Giant (2) | 4 Zenithra, 4 Celestine, 5 Sticks |
| Darkness Mace | Red Giant (2) | 4 Zenithra Blocks, 4 Zenithra, 5 End Rods |
| Hammer | Pulsar (3) | 6 Zenithra Blocks, 6 Celestine Blocks, 5 End Rods |
| Wings of Doom | Black Hole (4) | 4 Obscuridium Blocks, 3 Obscurite Blocks, 5 Breeze Rods |

The amounts are the same as in the old 5x5 crafting table recipes. Craft time:
10s (Sun), 8s (Red Giant), 4s (Pulsar), instant (Black Hole).

JEI
==========

The weapon recipes show up in the *Gravitational Craft* category that Simple Ores
adds to JEI (R / U on a weapon). Weapons Effect also adds an information page to
every weapon with the core it needs and what it does.

Development
==========

`./gradlew build` builds the mod. To test it with `./gradlew runClient`, put the
Simple Ores jar (26.2) in a `libs` folder: every jar there is loaded in the dev run.
