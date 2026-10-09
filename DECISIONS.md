# Decisions

## 2026-10-09 — Project Baseline

Decision: Start from a minimal NeoForge 1.21.1 Java project because the repository was empty.

## 2026-10-09 — Java Version

Decision: Use Java 21 based on the previous local environment verification recorded in `CHECKPOINTS.md`.

## 2026-10-09 — NeoForge and ModDevGradle

Decision: Pin NeoForge `21.1.256` and ModDevGradle `2.0.148` for the initial project baseline.

## 2026-10-09 — Required runtime dependencies

Decision: MineColonies, Structurize, and BlockUI remain required at runtime. Direct compile-time use is allowed only after exact artifacts and API signatures are validated.

## 2026-10-09 — Mixins

Decision: No mixins unless a required feature cannot be supported with a public API or event and the target method is verified against the pinned artifact.

## 2026-10-09 — Milestone 2 colony data ownership

Decision: Keep Imperium's empire state in Imperium-owned world SavedData, keyed by MineColonies identity (dimension + colony ID), rather than changing MineColonies internals or writing directly into colony-private NBT.

Rationale:
- `IColonyManager` offers server-side colony lookup by world/position and colony ID.
- `IColony` exposes stable public read methods including ID, name, center, world, citizen manager and dirty marking.
- No supported public extension slot for custom empire state was verified in the inspected interface excerpts.
- A separate saved-data layer reduces the risk of damaging existing colony saves and permits lazy/idempotent initialization.

## 2026-10-09 — Milestone 2 integration boundary

Decision: Prefer public MineColonies API types under `com.minecolonies.api`; do not import implementation types under `com.minecolonies.core` from Imperium gameplay code unless a documented limitation makes it unavoidable.

## 2026-10-09 — Exact artifacts

Decision: Update the candidate MineColonies/Structurize/BlockUI version notes using upstream 1.21.1 release metadata. Compilation against the actual artifacts remains unverified because this session has no local shell/build execution capability.
