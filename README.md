# Imperium: European Realms

A Minecraft Java Edition 1.21.1 NeoForge addon that expands MineColonies into a historical-fantasy European empire simulator.

## Requirements

- Minecraft Java Edition 1.21.1
- Java 21
- NeoForge 21.1.x
- MineColonies and its required dependencies
- The Gradle Wrapper in this repository

MineColonies is mandatory; Imperium is not a standalone colony simulator.

## Build and test

On Linux/macOS:

```bash
./gradlew --no-daemon clean build
```

On Windows:

```bat
gradlew.bat --no-daemon clean build
```

The JAR is written to `build/libs/`. Launch the development client with:

```bash
./gradlew runClient
```

GitHub Actions runs tests, compiles the mod, uploads the JAR, and performs a headless startup smoke test for the development client.

## First playable systems

- Existing and new MineColonies colonies are discovered through the public MineColonies API.
- Imperium persists per-colony state separately from MineColonies NBT, keyed by dimension and colony ID.
- The Imperial Chancery is registered as a real MineColonies hut and exposes the standard worker-assignment BlockUI window.
- The Diplomat is a registered MineColonies job with a worker AI and a once-per-day influence action.
- Low-tax, balanced and emergency-levy policies drive once-per-Minecraft-day tax collection from real MineColonies residents.
- Treasury crowns persist in Imperium's own SavedData.
- Colony managers can use `/imperium status`, `/imperium treasury status`, `/imperium treasury deposit <emeralds>` and `/imperium policy set <low_tax|balanced|emergency_levy>`.

Policy and treasury commands require the MineColonies `Manage Huts` permission. Deposits are server-side transactions and consume emeralds from the player's inventory.

## Current limitations

- The Chancery currently uses a vanilla spruce-plank texture.
- A custom Structurize blueprint pack for Chancery levels 1–5 has not yet been generated; without it, custom visual upgrades are not complete.
- Parliamentary rules and a dedicated policy BlockUI screen are planned for subsequent milestones.
- A headless client startup smoke test is not the same as a manual in-world save/reload test with a real MineColonies colony.
