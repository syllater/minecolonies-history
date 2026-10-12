# Milestone 3 — First Playable Vertical Slice

## Delivered systems
- MineColonies colonies serve as provinces; custom state is keyed by dimension + colony ID.
- Imperial Archive and Imperial Guard Tower with levels 1–5 and ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat professions plus specialist guard roles.
- Tier-scaled worker output, specialist training, hiring/guard capacity and saved per-worker cooldowns.
- Treasury, taxes, investment, policies, province focus/development and server-validated commands.
- Parliament proposals with faction votes, Emperor assent/veto and shared realm laws.
- Persistent realms/invitations, central treasury, audit history, supply routes, defensive orders, governors, military operations and cross-dimension strategy commands.
- BlockUI Imperial Ledger and graphical Imperial Theatre with server-authoritative snapshots, pan/zoom/recenter and province selection.
- English and Dutch translation coverage checked in CI.

## Latest full verification

[GitHub Actions workflow 38296167316](https://github.com/syllater/minecolonies-history/actions/runs/38296167316) passed all four jobs on source commit `a2bf703628c25379dd1c4f4acb1a8aa1e3dc457a`.

- EN/NL localization, tier progression, dependency/release metadata, cross-dimension command/help contracts and BlockUI ledger wiring passed.
- `./gradlew --no-daemon test build`, JAR SHA-256 manifest verification and self-checking release ZIP passed.
- Headless client startup reached `Sound engine started`.
- Dedicated server validated `jobs=6 buildings=2 guardTypes=3 modules=6` and loaded all `10/10` Structurize schematics.
- All six live GameTests passed:
  1. Daily tax/economy/realm persistence, worker cooldown and treasury/audit round-trip.
  2. Real MineColonies colony discovery, rename preservation and SavedData round-trip.
  3. Imperial Archive registration, jobs and tier 1–5 worker capacity.
  4. Imperial Guard Tower registration, military specialist jobs and tier 1–5 shared capacity.
  5. Owner/non-owner tax, policy, treasury and Parliament command authorization, valid Emperor veto, and insufficient-funds rollback.
  6. Cross-dimension invitations, owner-only join, duplicate-join denial and realm NBT round-trip.

Current JAR SHA-256: `351eeafb48e8e6bea538a701c55ae24f10b806df2a3ed15c8979c7701d13bebe`.

Artifacts:
- [Self-checking RC bundle](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768472386)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768327658)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11767998216)
- [Dedicated-server/blueprint log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768902441)
- [Client smoke log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768358033)

## Required before calling the project release-ready
- Build/upgrade both huts in a real survival world, hire workers/guards, verify sustained behavior and save/reload.
- Click through the BlockUI ledger and Imperial Theatre in a real client with a multi-province realm.
- Run a real two-player dedicated-server test for invitations, Emperor permissions, concurrent treasury operations and reconnect/save-reload.
- Review balance/usability and release notes.
- Explicitly choose a distribution license. This remains an RC, not a final public release.
