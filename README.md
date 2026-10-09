# Imperium: European Realms

A Minecraft Java Edition 1.21.1 NeoForge addon that expands MineColonies toward a historical-fantasy European empire simulator with a persistent imperial ledger, policies, treasury, scholarship and future parliament/military systems.

## Requirements

- Minecraft Java Edition 1.21.1.
- Java 21.
- NeoForge 21.1.x.
- MineColonies and its required runtime dependency set.
- The Gradle Wrapper included in this repository.

MineColonies is a required dependency; Imperium is not a standalone colony simulator.

## Build and test

On Linux/macOS:

    ./gradlew --no-daemon clean build

On Windows:

    gradlew.bat --no-daemon clean build

The mod JAR is written to build/libs/. To launch the development client:

    ./gradlew runClient

GitHub Actions runs unit tests, builds the JAR and then performs a headless development-client startup smoke test. Check the latest Actions run before distributing a JAR.

## Current gameplay slice

The server-side imperial ledger is keyed by dimension plus MineColonies colony ID and is saved by Imperium's own overworld SavedData. Existing MineColonies saves are initialized lazily and repeat scans do not reset treasury, policies or knowledge.

Player/server-authoritative commands:

- /imperium status — show treasury, population, tax rate, current policy, knowledge and stability.
- /imperium tax <0..25> — set a colony's tax rate.
- /imperium policy <balanced|mercantile|welfare|austerity> — choose an economic policy.
- /imperium invest <10..100000> — spend crowns in multiples of 10 for knowledge points.

Changing the economy requires colony hut-management permission or operator permission. Daily taxes are processed once per overworld day. The Philosopher job can produce knowledge at the Imperial Archive during the day, once per in-game minute.

## Imperial Archive status

The branch registers a MineColonies-compatible Imperial Archive hut anchor, building entry and Philosopher worker module. A crafting recipe and placeholder vanilla lectern model are included. The matching Structurize level-1 schematic pack has not yet been authored or verified, so this is not yet a fully buildable and upgradeable hut in survival until the blueprint is added and tested. The latest registry and AI changes also need a successful CI build before this slice can be called verified.

## Project documents

- PROJECT_GOAL.md: long-term project goal.
- ROADMAP.md: milestone plan.
- STATUS.md: verified state and blockers.
- COMPATIBILITY.md: dependency compatibility notes.
- docs/MINECOLONIES_INTEGRATION.md: integration boundary and persistence design.
- docs/MILESTONE_3.md: current vertical-slice design and acceptance notes.
