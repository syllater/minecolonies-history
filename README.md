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

GitHub Actions validates English/Dutch localization coverage and blueprint tier progression, builds/tests the mod, launches a dedicated server to validate the generated Structurize blueprints, and performs a headless client-startup smoke test. A green smoke test does not replace an in-world survival construction/save/reload playtest.

## Current systems

- MineColonies-aware colony identities and Imperium-owned persistent world state.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Upgraded schematics have visual tier progression: oriented eaves, archive roof ridge and added shelving, extra upper windows, lanterns, tower defensive iron bars, corner piers and battlements. CI checks tier-specific details as well as dimensions and valid hut anchors.
- Archive and guard-tower worker/guard hiring limits scale with building level, from one role slot at tier 1 up to five at tier 5.
- Archive profession output scales with the actual hut tier: Philosopher knowledge, Tax Collector efficiency and Diplomat influence yield 1 per work interval at tiers 1–2, 2 at tiers 3–4 and 3 at tier 5. Guard level-ups also yield tier-scaled Siege Engineering, Field Medicine or Cavalry Drill points; Military province focus doubles that training gain. Existing collection, influence and discipline caps remain enforced.
- Philosopher, Tax Collector and Diplomat professions; Imperial Siege Engineer, Field Medic and Cavalier specialist training, with Guard Tower tier scaling.
- Treasury, daily taxation, investment in knowledge, economic policies, province focus and development.
- Parliament bills with faction votes, imperial assent/veto and expiry.
- Faction approval, stability, legitimacy, strikes, revolts and separatist petitions driven by MineColonies citizen happiness and provincial conditions.
- Diplomatic influence and relationship scores between real MineColonies colonies.
- Federated realms with an Emperor/capital, member invitations, a separate central treasury, audit log, common tax/policy laws, provincial governors and regional events.
- Up to three concurrent military operations against distinct colony targets, military postures, persistent supply routes and temporary defensive orders.
- Strategic priority planning for routes and defense, plus a strategic map command and a full-screen interactive Imperial Theatre using real MineColonies colony center coordinates; pan, zoom, select provinces, and cycle the map projection among dimensions represented in the current realm. Map requests are server-authoritative, bounded, and rate-limited per player.
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
- A colony cannot target itself. The short campaign command resolves the target in the current dimension; use `/imperium campaign launch-at <type> <dimension> <colonyId>` to target a real colony in another dimension.
- `/imperium diplomacy improve-at <dimension> <colonyId>` improves relations with a real colony in another dimension; the existing `improve <colonyId>` form still uses the current dimension.
- War campaigns against a member province of the same realm are rejected, even when no separate bilateral relation score is set.
- Up to three operations may run concurrently, but each target may only be used by one active operation from that colony.
- Outcomes affect stability, legitimacy, training, treasury and relations; they do not automatically transfer territory.

## Imperial realms

Each real MineColonies colony can remain independent or join a multi-colony realm. The capital owner founds a realm, and the Emperor invites additional colonies. An invited colony's owner accepts while standing in that colony; invitations expire after seven in-game days. Non-capital provinces may leave.

- `/imperium empire status` — view Emperor, capital, member provinces and the central reserve.
- `/imperium empire found <name>` — found a realm using the current colony as capital.
- `/imperium empire invite <colonyId>` — invite a real MineColonies colony in the current dimension.
- `/imperium empire invite <dimension> <colonyId>` — invite a real MineColonies colony by dimension ID and colony ID, including colonies in another dimension.
- `/imperium empire join` — accept an invitation while in the invited colony.
- `/imperium empire leave` — leave from a non-capital province.
- `/imperium empire deposit <crowns>` — move crowns from local treasury to the shared imperial reserve.
- `/imperium empire withdraw <crowns>` — Emperor-only withdrawal to the current province.
- `/imperium empire route build <colonyId>` — build a supply route to an existing member province.
- `/imperium empire route build-at <dimension> <colonyId>` — build a route to a member province in another dimension.
- `/imperium empire defense at <dimension> <colonyId>` — issue a defensive order for a member province in another dimension.
- `/imperium governor appoint-at <dimension> <colonyId> <onlinePlayer>` and `/imperium governor dismiss-at <dimension> <colonyId>` — manage a member province's governor across dimensions.
- `/imperium empire route build-priority` — build a route to the highest-priority unsupplied province.
- `/imperium empire defense priority` — issue an emergency defensive order to the highest-risk eligible province.
- `/imperium empire routes`, `/imperium empire defenses` and `/imperium empire theatre` — inspect logistics and operations.
- `/imperium empire map` — show a coordinate-grid map derived from loaded MineColonies hut centers in the capital's dimension. Other-dimension and currently unloaded colonies are listed separately.
- `/imperium empire map <centerX> <centerZ> <blocksPerCell>` — show a fixed-coordinate viewport. Change X/Z to pan; use a smaller `blocksPerCell` to zoom in and a larger value to zoom out. Colonies outside the viewport are listed with their coordinates, so you can pan toward them.
- Press **I** to open the BlockUI Imperial Ledger, then select **Empire Map** to open the graphical theatre. Arrow keys pan, the mouse wheel and Zoom buttons adjust scale, **R** recentres, **Next dimension / Volgende dimensie** cycles through dimensions represented by realm provinces, and clicking a province marker shows its current stability, unrest and loyalty. Province membership and map data are resolved on the server; repeated snapshot requests are rate-limited.

The shared reserve is separate from provincial treasuries. Enacted empire-wide tax law remits a portion of already-collected provincial tax receipts instead of taxing those citizens a second time.

## Project documents

- `PROJECT_GOAL.md`: long-term project goal.
- `ROADMAP.md`: milestones and unfinished work.
- `STATUS.md`: current implementation and verification.
- `COMPATIBILITY.md`: dependency version notes.
- `docs/MINECOLONIES_INTEGRATION.md`: integration/persistence boundaries.
- `docs/MILESTONE_3.md`: playable systems and current limitations.
