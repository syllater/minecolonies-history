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

GitHub Actions builds/tests the mod, launches a dedicated server to validate the generated Structurize blueprints, and performs a headless client-startup smoke test. A green smoke test does not replace an in-world survival construction/save/reload playtest.

## Current systems

- MineColonies-aware colony identities and Imperium-owned persistent world state.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat professions; Imperial Siege Engineer, Field Medic and Cavalier specialist training.
- Treasury, daily taxation, investment in knowledge, economic policies, province focus and development.
- Parliament bills with faction votes, imperial assent/veto and expiry.
- Faction approval, stability, legitimacy, strikes, revolts and separatist petitions driven by MineColonies citizen happiness and provincial conditions.
- Diplomatic influence and relationship scores between real MineColonies colonies.
- Federated realms with an Emperor/capital, member invitations, a separate central treasury, audit log, common tax/policy laws, provincial governors and regional events.
- Up to three concurrent military operations against distinct colony targets, military postures, persistent supply routes and temporary defensive orders.
- Strategic priority planning for routes and defense, plus a strategic map command using actual MineColonies colony center coordinates.
- English and Dutch translations, a BlockUI imperial ledger and permission-checked server commands.

## Province commands

Stand inside your MineColonies colony and use:

- `/imperium province status`
- `/imperium province focus <agriculture|trade|scholarship|military|civic>`
- `/imperium province develop`

Province development costs 10 knowledge points. Focus changes daily economic/political effects or specialist military training.

## Strategic military operations

- `/imperium campaign status` lists recent operations and their resolution day.
- `/imperium campaign launch border_patrol <colonyId>` costs 25 crowns and resolves after one in-game day.
- `/imperium campaign launch relief_expedition <colonyId>` costs 75 crowns and 5 diplomatic influence and resolves after two days.
- `/imperium campaign launch war_campaign <colonyId>` costs 150 crowns and 10 influence, requires at least 3 specialist military training points, and resolves after three days.
- A colony cannot target itself; the selected MineColonies colony must be resolvable in the current dimension for the campaign commands.
- Up to three operations may run concurrently, but each target may only be used by one active operation from that colony.
- Outcomes affect stability, legitimacy, training, treasury and relations; they do not automatically transfer territory.

## Imperial realms

Each real MineColonies colony can remain independent or join a multi-colony realm. The capital owner founds a realm, and the Emperor invites additional colonies. An invited colony's owner accepts while standing in that colony; invitations expire after seven in-game days. Non-capital provinces may leave.

- `/imperium empire status` — view Emperor, capital, member provinces and the central reserve.
- `/imperium empire found <name>` — found a realm using the current colony as capital.
- `/imperium empire invite <colonyId>` — invite a real MineColonies colony in the current dimension.
- `/imperium empire join` — accept an invitation while in the invited colony.
- `/imperium empire leave` — leave from a non-capital province.
- `/imperium empire deposit <crowns>` — move crowns from local treasury to the shared imperial reserve.
- `/imperium empire withdraw <crowns>` — Emperor-only withdrawal to the current province.
- `/imperium empire route build <colonyId>` — build a supply route to an existing member province.
- `/imperium empire route build-priority` — build a route to the highest-priority unsupplied province.
- `/imperium empire defense priority` — issue an emergency defensive order to the highest-risk eligible province.
- `/imperium empire routes`, `/imperium empire defenses` and `/imperium empire theatre` — inspect logistics and operations.
- `/imperium empire map` — show a coordinate-grid map derived from loaded MineColonies hut centers in the capital's dimension. Other-dimension and currently unloaded colonies are listed separately.

The shared reserve is separate from provincial treasuries. Enacted empire-wide tax law remits a portion of already-collected provincial tax receipts instead of taxing those citizens a second time.

## Project documents

- `PROJECT_GOAL.md`: long-term project goal.
- `ROADMAP.md`: milestones and unfinished work.
- `STATUS.md`: current implementation and verification.
- `COMPATIBILITY.md`: dependency version notes.
- `docs/MINECOLONIES_INTEGRATION.md`: integration/persistence boundaries.
- `docs/MILESTONE_3.md`: playable systems and current limitations.
