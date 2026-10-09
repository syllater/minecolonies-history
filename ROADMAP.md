# Roadmap

## Milestone 0 — Repository and Compatibility

Status: baseline and pinned 1.21.1 dependency source inspection complete. Gradle and client-smoke outcomes are recorded from GitHub Actions.

## Milestone 1 — Technical Foundation

Status: NeoForge entrypoint, Gradle config, English/Dutch translations, JUnit infrastructure and CI verification workflow.

## Milestone 2 — MineColonies Integration

Implemented: public server-side colony lookup, dimension+colony identity, Imperium-owned overworld SavedData, idempotent discovery and tests.
Verification: the integration branch has passed the Gradle build/test and headless client smoke workflow. Manual save/reload with a player-created colony is still needed for full runtime confidence.

## Milestone 3 — Initial Economy and Policies

Implemented: persistent treasury/tax rate/policy; daily-unique tax math with policy upkeep; server-authoritative commands gated by MineColonies Manage Huts; English/Dutch strings and unit tests.
Verification: CI passed the build/test and client smoke workflow for the corrected economy revision.

## Milestone 4 — Parliament and Politics

Implemented on `milestone-4/parliament-politics`: constitutional empire label; persisted proposals and votes; one vote per colony member; one full in-game day debate; majority decisions; three laws with economic effects; translations and unit tests.
Pending: verify the newest branch build/test and client smoke workflow.

## Milestone 5 — Factions, Citizen Approval and Unrest

Implemented on `milestone-5/factions-approval`:
- Persisted approval, unrest and four faction support shares per colony.
- Daily political simulation uses MineColonies colony-wide happiness (public API, 0–10 scale) and responds to taxes/policies.
- Shares are bounded and normalized to total 100.
- Daily treasury and politics are advanced by the existing colony discovery pass after day zero.
- Added `/imperium politics` status view and unit tests.

Next:
- Expose a proper player-facing management GUI, while keeping commands as admin/debug access.
- Add emperor office-holder rules and improve faction-specific behavior.
- Add a truly registered custom MineColonies worker/hut with a tested 1.21.1 AI/view/block/registry/schematic implementation.
- Continue military units, buildings, multiplayer save/load and release hardening.

## Milestone 6 — Citizens and Economy

Add complete custom MineColonies job integrations including job AI, view, building/module support and registration for Tax Collector, Philosopher and Diplomat. Do not claim a role exists until actual MineColonies can assign/work it.

## Milestone 7 — Military

Implement real registered units/jobs for Imperial Field Medic, Siege Engineer and Imperial Cavalier, including server authoritative combat/AI and guard compatibility.

## Milestone 8 — Buildings and Visual Progression

Add a genuine schematic-backed imperial building, then level-based visual and functional progression from L1 through L5 using Structurize and appropriate BlockUI interfaces.

## Milestone 9 — Empire Simulation

Domestic factions, approval/unrest events, provinces, diplomacy and grand strategy wars.

## Milestone 10 — Multiplayer and Release

Dedicated server, migration/save/reload, network validation, translations, reproducible JAR and documented tested dependency versions.
