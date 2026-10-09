# Imperium: European Realms

A Minecraft Java Edition 1.21.1 NeoForge addon that expands MineColonies into a historical-fantasy European empire simulator. The current source includes an imperial treasury, policy bills and Parliament, faction approval/unrest, diplomacy, scholarly/tax administration, and specialist military roles.

## Requirements

- Minecraft Java Edition 1.21.1.
- Java 21.
- NeoForge 21.1.x.
- MineColonies with its required runtime dependency set.
- Python 3.9+ for the build-time Structurize blueprint generator. It uses only Python's standard library; no API keys or pip packages are required.
- The Gradle Wrapper included in this repository.

MineColonies is a required dependency; Imperium is not a standalone colony simulator.

## Build and test

On Linux/macOS:

    ./gradlew --no-daemon clean build

On Windows:

    gradlew.bat --no-daemon clean build

The mod JAR is written to `build/libs/`. To launch the development client:

    ./gradlew runClient

The Gradle build generates the custom Structurize NBT schematics from `tools/generate_blueprints.py` before resources are packaged. That source generator creates five levels each for the Imperial Archive and Imperial Guard Tower. GitHub Actions runs tests, builds the JAR and then performs a headless development-client startup smoke test. Check the latest Actions run before distributing a JAR.

## Colony integration and economy

Imperium keeps a persistent `EmpireState` in Imperium-owned overworld SavedData keyed by dimension plus MineColonies colony ID. It does not mutate MineColonies-private NBT. Discovery is idempotent and recognizes colonies in existing worlds.

The ledger tracks crowns, taxation, economic policy, scholarship, tax collection efficiency, stability, legitimacy, unrest, four faction-approval scores, diplomatic influence, bilateral relations, parliamentary bills and specialist military-training scores.

## Commands

- `/imperium status` — inspect the imperial ledger and treasury.
- `/imperium tax <0..25>` — propose a tax rate for parliamentary approval.
- `/imperium policy <balanced|mercantile|welfare|austerity>` — submit a policy bill.
- `/imperium parliament status` — list bills, votes and resolution audit details.
- `/imperium parliament assent <id>` or `/imperium parliament veto <id>` — resolve a bill as Emperor.
- `/imperium politics` — view faction approval, legitimacy and civil unrest.
- `/imperium diplomacy status` — review diplomatic influence and relations.
- `/imperium diplomacy improve <colonyId>` — spend influence to improve relations with a real MineColonies colony in the same dimension.
- `/imperium army` — inspect Siege Engineering, Field Medicine and Cavalry Drill progress.
- `/imperium invest <10..100000>` — invest crowns in knowledge.

Mutating commands execute on the server and validate colony access/permissions. Policy and tax changes are handled through Parliament; they are not silently applied client-side.

## MineColonies professions and buildings

- **Imperial Archive:** Philosophy, tax collection and diplomatic dispatches; upgrade schematics for levels 1–5 are generated into the `imperium_european` Structurize pack.
- **Imperial Guard Tower:** native MineColonies guard management with the Imperial Siege Engineer, Imperial Field Medic and Imperial Cavalier roles; levels 1–5 are generated into the same pack.
- **Philosopher:** builds knowledge at a controlled work interval.
- **Tax Collector:** raises persisted tax collection efficiency up to 25%.
- **Diplomat:** generates influence used to improve inter-colony relations.
- **Imperial military roles:** inherit the corresponding MineColonies guard AI/equipment flows and record specialist training on level-up.

The generated schematics are original, compact European-inspired structures using a declared block palette. This source generator makes the packed NBT reproducible. A clean CI build and client startup are necessary checks, but an actual in-world builder/upgrade/save-reload playtest is still required before calling the release fully playtested.

## Project documents

- `PROJECT_GOAL.md`: long-term goal.
- `ROADMAP.md`: milestone plan.
- `STATUS.md`: verified state and current blockers.
- `COMPATIBILITY.md`: dependency and source compatibility notes.
- `docs/MINECOLONIES_INTEGRATION.md`: integration boundary and persistence design.
- `docs/MILESTONE_3.md`: first playable systems and building blueprint notes.
