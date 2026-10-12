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

## 2026-10-10 — Temporary defensive orders

Decision: Give the Emperor a timed defensive-order mechanism, distinct from standing military posture. One active order is allowed per province; it costs 50 central crowns, lasts seven in-game days and adds +20 readiness when the province is targeted.

Rationale: Strategic defense should be an explicit command choice, not just an automatic passive score. Orders are persistent, server-authoritative and expire during the daily turn.


## 2026-10-11 — Imperial Theatre dimension selection

Decision: The map client may request a display dimension, but the server resolves it only when at least one province in the player's current realm exists in that dimension. Empty or invalid requests fall back to the realm capital's dimension.

Rationale:
- A realm can span multiple Minecraft dimensions, while a single X/Z projection cannot meaningfully combine coordinates from unrelated dimension spaces.
- The client request is a presentation preference only and never contains a realm ID or membership list.
- Province names, membership, coordinates and metrics remain sourced from server state.
- A regression test covers a valid member dimension, empty/blank requests and rejection of a dimension absent from the realm.

## 2026-10-11 — Cross-dimension realm administration

Decision: Supply-route construction, temporary defensive orders, and governor appointment/dismissal use explicit dimension + colony ID variants. Those variants resolve the target with the server's MineColonies API and re-check that the target is a member province and the caller is the Emperor or a server operator.


## 2026-10-11 — Strategic map request rate limiting

Decision: Limit authenticated map snapshot requests to one per player per 10 overworld ticks (0.5 seconds at normal game speed). Expire idle limiter entries after 24,000 ticks and use the shared overworld clock so dimension changes cannot reset the cooldown.

Rationale:
- Map requests traverse realm membership and MineColonies colony lookups and should not be rebuilt on every packet from a modified client.
- The client sends a request preference only; this limit does not alter realm data or gameplay state.
- A small pure-Java utility keeps the cooldown testable without a live Minecraft world.


## 2026-10-12 — Add live GameTest persistence gate

Decision: Keep a small, headless Minecraft GameTest in CI for the most important SavedData economy/realm serialization contract, in addition to pure unit tests and client/server smoke launches.

Rationale:
- Unit tests are fast, but an actual GameTest server exercises Minecraft's CompoundTag and SavedData code in its mapped runtime.
- The test uses synthetic colony identity keys and does not create fake MineColonies colonies or write into MineColonies internals.
- Physical hut placement, worker AI in survival, GUI click-through and two-player network/permission acceptance remain manual release gates; passing the GameTest is not used to certify those behaviors.


## 2026-10-12 — Extend MineColonies shared hut block-entity type through NeoForge

Decision: Use NeoForge's `BlockEntityTypeAddBlocksEvent` to add the custom Imperial Archive and Imperial Guard Tower blocks to `minecolonies:colonybuilding`.

Rationale: MineColonies constructs its shared hut block-entity type from its own internal hut list. An addon block that extends `AbstractBlockHut` but is absent from that list is rejected as an invalid block-entity state at placement time. The official mod-bus event safely extends the type's valid-block set; this avoids mixins/reflection and was verified by real Minecraft GameTests.

## 2026-10-12 — Runtime integration acceptance boundary

Decision: Keep the release candidate open until manual survival and two-player acceptance is completed. Live GameTests now prove real colony lookup, hut anchor/building registration, registered job assignment and tier capacity, but they do not claim to build entire Structurize schematics in survival, observe workers over multiple game days, exercise every GUI interaction, or verify genuine multiplayer permissions/concurrency/reconnect.
