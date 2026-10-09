# Milestone 3 — First Playable Vertical Slice

## Economic model

Each MineColonies colony is associated with an Imperium-owned EmpireState stored in overworld SavedData. The identity is the dimension resource ID plus MineColonies colony ID, so a changed colony name does not create a new record.

- Treasury is stored in crowns and capped at 1,000,000,000.
- Tax rate is 0–25 percent; default is 5 percent.
- Daily tax turns are keyed to overworld game time and persisted per colony to prevent duplicate charges after reloads.
- Policies: balanced (baseline), mercantile (+25% tax yield), welfare (-25% tax yield and +2 stability/day), austerity (+50% tax yield and -2 stability/day).
- Crowns may be invested in knowledge at a rate of 10 crowns per knowledge point.
- Stability ranges from 0 to 100.
- The Philosopher generates one knowledge point at most every 1,200 game ticks while at an Imperial Archive and during daytime.

These are deliberately modest first-slice rules. Further production, upkeep, corruption, events, citizen happiness and political approval will be layered in later milestones.

## Commands

- /imperium status
- /imperium tax <0..25>
- /imperium policy <balanced|mercantile|welfare|austerity>
- /imperium invest <10..100000>

Command mutations execute on the server. Financial/policy mutations require operator permission or MineColonies colony MANAGE_HUTS permission. Responses are localized in English and Dutch.

## MineColonies integration

The Philosopher is a custom JobEntry using MineColonies' AI state machine and a WorkerBuildingModule on the Imperial Archive. The Archive hut uses the public MineColonies block/building registry pattern. The crafting item has a recipe and placeholder model.

## Schematic blocker

A custom MineColonies hut needs its matching Structurize blueprint pack and metadata to complete construction through the builder. This branch currently declares schematic name imperialarchive but does not yet contain a proven level-1 blueprint. The building registry is therefore not considered a complete playable hut until schematic assets and an in-world build test are added.

## Acceptance tests

- EmpireStateTest covers defaults, daily tax idempotency, policy effect, tax bounds, investment and scholarship timing.
- ./gradlew test build must pass.
- Headless ./gradlew runClient must reach the startup marker.
- Runtime acceptance also needs a world save/reload and an actual MineColonies Builder completing the Imperial Archive blueprint.
