# Imperium: European Realms

A Minecraft Java Edition 1.21.1 NeoForge addon that extends MineColonies toward a European empire simulator.

## Requirements

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.x
- MineColonies and its required dependency set (required, not optional)

## Build

```bash
./gradlew --no-daemon clean build
```

The distributable JAR is written to `build/libs/`.

## Launch the development client

```bash
./gradlew runClient
```

## In-game controls

Press **O** to open the BlockUI Imperial Ledger. Its buttons send normal `/imperium` commands to the server. The server verifies colony membership and MineColonies permissions before applying changes.

Examples:

- `/imperium status`
- `/imperium taxes rate 10`
- `/imperium politics`
- `/imperium parliament status`
- `/imperium parliament propose public_works_act`
- `/imperium parliament vote yes`
- `/imperium parliament resolve`
- `/imperium emperor status|claim|appoint <player>|abdicate`
- `/imperium diplomacy status`
- `/imperium diplomacy offer <colonyId> alliance`
- `/imperium diplomacy accept|decline`

## Current implemented systems

- MineColonies colony lookup and separate Imperium SavedData indexed by dimension + colony ID.
- Treasury, tax rates and administrative policy with daily taxation/upkeep.
- Parliament proposals, votes, majority outcomes and three initial acts.
- Daily citizen-approval/unrest simulation and four domestic faction support shares.
- English and Dutch player-facing strings.
- Emperor office and succession history.
- Bilateral diplomatic treaties (friendship, trade, non-aggression, alliance) requiring acceptance by the target colony.
- BlockUI ledger controls for emperor office and pending treaty offers.

## Project documents

- `PROJECT_GOAL.md`: long-term goal.
- `ROADMAP.md`: implementation plan.
- `STATUS.md`: current verification state.
- `COMPATIBILITY.md`: dependency baseline and API references.
- `docs/MINECOLONIES_INTEGRATION.md`: colony integration design.
