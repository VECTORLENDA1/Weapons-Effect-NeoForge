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
| Blindness Dagger | Sun (1) | 2 Nexalite Blocks, 8 Ink Sacs, 2 Fermented Spider Eyes, 2 Sticks |
| Swiftness Dagger | Sun (1) | 2 Astralite Blocks, 16 Sugar, 8 Feathers, 1 Rabbit's Foot, 2 Sticks |
| Poison Sword | Sun (1) | 3 Nexalite Blocks, 8 Spider Eyes, 4 Poisonous Potatoes, 2 Pufferfish, 2 Sticks |
| Ice Sword | Sun (1) | 3 Astralite Blocks, 4 Blue Ice, 16 Snowballs, 2 Sticks |
| Lance | Sun (1) | 5 Nexalite Blocks, 3 Iron Blocks, 4 Leather, 4 Sticks |
| Wither Sword | Sun (1) | 4 Nexalite Blocks, 1 Wither Skeleton Skull, 4 Wither Roses, 8 Soul Sand, 2 Sticks |
| Fire Sword | Sun (1) | 4 Ignithra Blocks, 6 Blaze Rods, 8 Magma Blocks, 4 Fire Charges |
| Gimli's Axe | Red Giant (2) | 4 Zenithra Blocks, 4 Celestine Blocks, 4 Iron Blocks, 2 Gold Blocks, 4 Sticks |
| Darkness Mace | Red Giant (2) | 6 Zenithra Blocks, 4 Echo Shards, 16 Sculk, 8 Obsidian, 2 Breeze Rods |
| Hammer | Pulsar (3) | 10 Zenithra Blocks, 10 Celestine Blocks, 4 Diamond Blocks, 8 Iron Blocks, 2 Anvils, 4 End Rods |
| Wings of Doom | Black Hole (4) | 8 Obscuridium Blocks, 6 Obscurite Blocks, 3 Nether Stars, 1 Elytra, 8 Dragon's Breath, 8 Echo Shards, 8 Breeze Rods |

Craft time: 10s (Sun), 8s (Red Giant), 4s (Pulsar), instant (Black Hole).

JEI
==========

The weapon recipes show up in the *Gravitational Craft* category that Simple Ores
adds to JEI (R / U on a weapon). Weapons Effect also adds an information page to
every weapon with the core it needs and what it does.

Development
==========

`./gradlew build` builds the mod. To test it with `./gradlew runClient`, put the
Simple Ores jar (26.2) in a `libs` folder: every jar there is loaded in the dev run.
