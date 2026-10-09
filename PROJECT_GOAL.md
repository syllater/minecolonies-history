# Project Goal

Build **Imperium: European Realms** as a real Minecraft Java Edition 1.21.1 NeoForge mod that extends MineColonies with a European historical-fantasy empire simulation.

## Long-term target

- A successful Gradle build and generated mod JAR.
- A successful verified attempt to start the development client with `./gradlew runClient`.
- MineColonies as a required dependency and deeply integrated with real physical colonies.
- Structurize support for building schematics and visual progression, and BlockUI for usable player interfaces where compatible.
- Server-authoritative gameplay that works in singleplayer and multiplayer.
- English and Dutch translations.
- No runtime dependency on external AI API keys.
- Parliament and emperor governance; treasury, taxation, provincial administration, happiness/factions, strikes and unrest, diplomacy and grand-strategy military systems.
- Complete professions including Diplomat, Tax Collector and Philosopher; military professions including Siege Engineer, Imperial Field Medic and Imperial Cavalier.
- Existing worlds and colonies must remain usable; no requirement to recreate a MineColonies colony or world.

## Execution policy

The user explicitly authorized continuous execution through all remaining milestones without stopping to request separate approvals. Continue sequentially toward the best attainable finished mod. Keep work on feature branches, make durable checkpoints, run automated tests, build the JAR and smoke-test the client. Do not merge feature branches into `main` without permission.

Do not claim success that has not been verified. A CI client startup check does not replace an in-world test of a real colony and save/reload behavior. Record limitations rather than hiding them.

## Technical baseline

- Minecraft Java Edition 1.21.1
- Java 21
- NeoForge 21.1.x
- MineColonies required
- English code identifiers and English/Dutch user-facing translations
