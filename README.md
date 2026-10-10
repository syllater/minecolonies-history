# Imperium: European Realms

A Minecraft Java Edition 1.21.1 NeoForge addon that extends MineColonies into a historical-fantasy European empire simulator.

## Requirements

- Minecraft Java Edition 1.21.1 and Java 21.
- NeoForge 21.1.x.
- MineColonies and its required runtime dependencies (Structurize, BlockUI, Domum Ornamentum and MultiPiston).
- Python 3.9+ for deterministic generation and validation of original Structurize blueprints.

## Build and test

Linux/macOS:

```bash
./gradlew --no-daemon clean test build
```

Windows:

```bat
gradlew.bat --no-daemon clean test build
```

To launch a development client:

```bash
./gradlew runClient
```

The GitHub Actions workflow builds and tests the mod, launches a dedicated server to validate the generated Structurize blueprints, and performs a headless client-startup smoke test. A green smoke test is not a substitute for an in-world survival construction/save/reload playtest.

## Current systems

The current source includes:
- MineColonies-aware colony identities and Imperium-owned persistent world state.
- A five-tier Imperial Archive with Philosopher, Tax Collector and Diplomat worker roles.
- A five-tier Imperial Guard Tower with Imperial Siege Engineer, Field Medic and Cavalier roles.
- Treasury, daily taxation, investment in knowledge and four economic policies.
- Parliament bills with faction votes, imperial assent/veto and expiry.
- Faction approval, stability, legitimacy, strikes and revolts driven by MineColonies citizen happiness.
- Diplomatic influence and relationship scores between actual MineColonies colonies.
- Military training tracks and a development ledger.
- Abstract provincial administration: settlement/county/duchy/principality/kingdom tiers, five provincial focuses, focus effects and knowledge-funded development.
- English and Dutch translations, a BlockUI imperial ledger and permission-checked server commands.
- Ten original generated Structurize blueprints covering the Archive and Guard Tower at levels 1–5.

## Province commands

Stand inside your MineColonies colony and use:
- `/imperium province status`
- `/imperium province focus <agriculture|trade|scholarship|military|civic>`
- `/imperium province develop`

Province development costs 10 knowledge points. Province focus changes daily economic/political effects or specialist military training. The GUI exposes the most common province actions; the server remains authoritative.

## Strategic military operations

- `/imperium campaign status` lists recent operations and their resolution day.
- `/imperium campaign launch border_patrol <colonyId>` costs 25 crowns and resolves after one in-game day.
- `/imperium campaign launch relief_expedition <colonyId>` costs 75 crowns and 5 diplomatic influence and resolves after two days.
- `/imperium campaign launch war_campaign <colonyId>` costs 150 crowns and 10 influence, requires at least 3 specialist military training points, and resolves after three days.
- Only real MineColonies colonies in the same dimension are valid targets. A realm can run one operation at a time; campaigns and consequences persist across save/reload.
- War outcomes affect stability, legitimacy, training, treasury and relations, but never automatically transfer territory.

## Project documents

- `PROJECT_GOAL.md`: long-term project goal.
- `ROADMAP.md`: milestones and unfinished work.
- `STATUS.md`: current implementation and verification.
- `COMPATIBILITY.md`: dependency version notes.
- `docs/MINECOLONIES_INTEGRATION.md`: integration and persistence boundaries.
- `docs/MILESTONE_3.md`: playable systems and their current limits.
