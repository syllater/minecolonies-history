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

## 2026-10-10 — Imperial supply routes

Decision: Phase one logistics uses direct routes from the empire capital to a member province, not a graph/path network. Routes cost 100 central crowns to construct and 2 crowns/day to maintain.

Rationale:
- The direct model fits the current multi-colony identity and can be tested without relying on world-coordinate assumptions.
- Active routes (condition >= 40/100) add +8 launch readiness from the routed province and +12 defensive readiness when that province is targeted. The capital is intrinsically supplied.
- A route starts at 100 condition, repairs +5 per paid daily upkeep and wears -10 on unpaid upkeep.
- Routes are stored in Imperium SavedData and removed if the province leaves the realm.

## 2026-10-10 — Military readiness and supply-route review

- Defensive doctrine and current supply-route condition are evaluated when an operation resolves; launch readiness and source logistics are saved with the operation.
- Supply-route CI initially caught a missing command-layer import and an incorrect unit-test treasury expectation. Both are corrected in the follow-up commit.
