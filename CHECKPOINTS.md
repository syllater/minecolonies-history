# Checkpoints

## 2026-10-10 — Imperial audit trail and governors

### Repository
- Branch: `milestone-2/colony-integration`
- Last fully green feature revision: `489fcf9311883230d1b6b50038ad7560ad59b35b`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38062626937

### Verified on that revision
- Gradle build and unit tests: passed.
- Dedicated server and Structurize schematic validation: passed.
- Headless client startup: passed.
- Mod JAR artifact uploaded by CI.

### Implemented
- Empire-wide tax and policy laws propagate from the capital parliament; joining provinces inherit enacted laws.
- A 10% remittance transfers already-collected provincial taxes into the central treasury while an imperial tax law is active.
- SavedData schema 15 added bounded (100 entries/realm) audit logs; schema 16 now additionally persists governor appointments.
- `/imperium empire audit` and BlockUI audit access show recent financial, legal and membership changes.
- An Emperor/server operator can appoint or dismiss a player as governor for a non-capital province via `/imperium governor appoint|dismiss`; appointments are saved and have daily political benefits.

### Pending on the follow-on commit
- A focused unit test for the daily governor effect has been added; it awaits CI verification alongside the latest command, GUI and data schema update.

### Known gaps
- Automated smoke tests do not replace survival-world testing of structure placement, worker hiring, upgrades 1–5 and save/reload.
- Real multiplayer testing and strategic supply-route/map features remain open.

### Next
Verify the current full workflow, then implement regional events and repeat the same test gates.

## 2026-10-10 — Multi-front military doctrine

- Added three concurrent strategic-operation slots per colony, with duplicate-target protection.
- Added persistent balanced, offensive and defensive posture.
- Offensive posture contributes +12 readiness when an operation launches; defensive posture contributes +20 when defending against an operation.
- Posture changes affect future operations; ongoing operations keep their stored launch readiness.
- Incremented SavedData schema to 19; older records migrate to balanced posture by default.
- Added command support and English/Dutch translations.
- Verification is pending for this new commit; do not treat it as green until CI finishes.

## 2026-10-10 — Supply routes and logistics

- Added a persistent direct capital-to-member-province route model with condition and upkeep day.
- Construction costs 100 crowns from the imperial reserve; active routes cost 2 crowns per in-game day. Unpaid routes degrade by 10 condition/day; funded routes repair by 5/day.
- Routes below 40/100 condition no longer provide military logistics bonuses. The capital is intrinsically supplied.
- Added /imperium empire routes and /imperium empire route build <colonyId>. Only the Emperor or a server operator may spend central funds on construction.
- Connected route upkeep to the once-per-day authoritative economic turn.
- SavedData schema increased to 20, with existing saves defaulting to no routes.
- Added upkeep/build/maintenance tests, BlockUI access and English/Dutch translations. CI for this commit is pending.

## 2026-10-10 — First supply-route CI failure

- The Gradle compile failed because `ImperiumCommands` referenced `ImperialSupplyRoute` without importing the class. A unit-test assertion also expected a full treasury balance after depositing only the construction cost; the correct remaining amount is zero.
- Follow-up fixes are committed; retest with the new CI run.

## 2026-10-10 — Temporary defensive orders

- Added a seven-day defensive order per province, costing 50 central crowns and providing +20 defensive readiness while active.
- Orders are stored per realm, removed when a province leaves, and expired on the authoritative daily turn.
- Only the Emperor or server operator can issue orders spending central funds.
- Added command/status listing, BlockUI access, English/Dutch translations and tests. Verification pending.

## 2026-10-10 — Strategic theatre overview

- Added `/imperium empire theatre` to show a realm-level snapshot of all tracked provinces and pending operations.
- Added route condition and temporary defensive-order status per province, including current readiness infrastructure.
- Added BlockUI entry and English/Dutch translations. Coordinate map remains future work; this is the low-risk text-first overview.
