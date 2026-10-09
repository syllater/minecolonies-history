# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Repository state

- Repository: https://github.com/syllater/minecolonies-history
- Current branch: milestone-2/colony-integration
- Main remains unmerged; do not merge without user approval.
- Code edits are written through the connected GitHub repository API. Build/test/client execution is performed by GitHub Actions.

## Current work

Milestone 3 — First playable vertical slice (in progress).

## Implemented source

- MineColonies public API colony lookup and idempotent colony discovery.
- Versioned Imperium-owned SavedData keyed by dimension plus colony ID.
- Treasury, tax rate (0–25%), economic policies, stability, knowledge points and last processed daily turn.
- Daily tax processing from the loaded MineColonies population, once per overworld game day.
- Server-side /imperium status, /imperium tax, /imperium policy and /imperium invest command handlers.
- Operator/colony hut-management permission checks for money/policy modifications.
- English and Dutch translation files for ledger commands and Archive/Philosopher labels.
- MineColonies registry entries for the Philosopher profession and Imperial Archive building/hut anchor.
- Philosopher AI that walks to the Archive and records scholarship at most once per 1,200 ticks during daytime.
- Crafting recipe and placeholder vanilla block/item visuals for the Archive.
- Tests for colony identity, initialization defaults, tax idempotence, policy effects, investment, scholarship interval and treasury bounds.

## Verification

- Verified base: CI run 37925143022 passed build/tests and headless client smoke on an earlier Milestone 2 baseline.
- A later Milestone 3 revision failed compilation because the Philosopher AI referred to missing inherited helpers and incorrectly annotated decide() as an override.
- The AI was updated to define those helpers locally and remove the invalid override.
- That correction still needs fresh CI verification. Do not claim the current milestone-3 revision compiles or that the current JAR is deployable until the newest Actions result passes.

## Known incomplete items

- The Imperial Archive's Structurize blueprint pack is not included/verified, so the registered building is not yet a complete survival-buildable hut despite its block, building registry, AI and recipe.
- Command UI currently uses vanilla commands; a dedicated BlockUI GUI has not been added.
- No in-world test has verified a player building the Archive, assigning a Philosopher and saving/reloading a world.
- Diplomat, Tax Collector, army units, parliament, province simulation and diplomacy remain later milestone work.

## Next actions

1. Inspect fresh Actions compile/test result and fix all real failures.
2. Pass ./gradlew test build.
3. Pass the headless ./gradlew runClient smoke test after a successful build.
4. Add and verify the level-1 Structurize blueprint and actual worker assignment loop.
5. Continue into parliament, economy, diplomacy and military features without misreporting incomplete milestones as finished.
