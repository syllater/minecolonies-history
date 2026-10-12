# Milestone 3 — First Playable Vertical Slice

## Delivered systems
- Real MineColonies colonies serve as provinces; custom state is keyed by dimension + colony ID.
- Imperial Archive and Imperial Guard Tower with levels 1–5 and ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat professions plus specialist guard roles.
- Archive professions scale their output by hut tier, with existing caps and interval throttles retained.
- Guard Tower tier increases specialist training points on guard level-up; Military province focus doubles that gain.
- Treasury, taxation, investment, economic policy, province focus/development and server-validated commands.
- Parliament proposals with faction votes, Emperor assent/veto and shared realm laws.
- Persistent realm membership, invitations, central treasury, audit entries, supply routes, defensive orders and governors.
- Up to three concurrent military operations with postures, specialist training and deterministic priority actions.
- BlockUI Imperial Ledger with shortcuts for politics, economy, development and realm systems.
- Chat-based strategic overview plus graphical Imperial Theatre with server-authoritative province snapshots, pan/zoom/recenter, province selection and a server-validated dimension selector.
- Cross-dimension commands for routes, defensive orders, governor appointment/dismissal, diplomacy, campaigns and separatist-petition resolution.
- Per-player map-request throttling with stale limiter cleanup and regression coverage.
- English and Dutch translation coverage checked in CI.

## Latest candidate verification

Full green workflow: [38228182714](https://github.com/syllater/minecolonies-history/actions/runs/38228182714) on source commit `61d7f9c9cb66fca8d9f96f9773878b6b196c43be`.

All four CI jobs passed:
- EN/NL localization (298 keys), generated tier progression, dependency/release metadata, cross-dimension command/help and BlockUI ledger wiring (40 buttons/38 handlers/2 dedicated actions).
- `./gradlew --no-daemon test build`, JAR SHA-256 generation/verification, and self-checking RC ZIP assembly.
- Headless client startup reached `Sound engine started`.
- Dedicated server validated `jobs=6 buildings=2 guardTypes=3 modules=6` and loaded all `10/10` Structurize schematics.
- Four live GameTests passed: daily tax/economy/realm persistence; discovery and serialization of a real MineColonies colony; real Archive registration plus the three citizen jobs and tier-scaled hiring; and real Guard Tower registration plus the three military specialist jobs and tier-scaled shared capacity.

Current JAR SHA-256: `4d44a38535747159a93d22ca46744852931d1929e3dfefc02c37747333685bdb`.

Artifacts:
- [Self-checking release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737131416)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737226311)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737920417)
- [Dedicated-server log](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737576125)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737760664)

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Inspect tier silhouettes and tier-scaled profession/military progression in-game, then prepare release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.


## Latest reproducible package (2026-10-12)

The latest full green verification is [workflow 38228182714](https://github.com/syllater/minecolonies-history/actions/runs/38228182714) on commit `61d7f9c9cb66fca8d9f96f9773878b6b196c43be`. All four jobs passed. The candidate JAR SHA-256 is `4d44a38535747159a93d22ca46744852931d1929e3dfefc02c37747333685bdb`. The current candidate bundle is [here](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737131416), with the [JAR/checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737226311).
