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

Full green workflow: [38226482725](https://github.com/syllater/minecolonies-history/actions/runs/38226482725) on source commit `337a51000fe0889b854cc607994cab5d1b05f447`.

All four CI jobs passed:
- EN/NL translation, blueprint tier progression, dependency/release metadata and cross-dimension command validation.
- `./gradlew --no-daemon test build`, JAR SHA-256 generation/verification, and self-checking RC ZIP assembly.
- Headless client startup reached `Sound engine started`.
- Dedicated server validated `jobs=6 buildings=2 guardTypes=3 modules=6` and loaded all `10/10` Structurize schematics.
- Four live GameTests passed: daily tax/economy/realm persistence; discovery and serialization of a real MineColonies colony; real Archive building registration plus the three citizen jobs and tier-scaled hiring; and real Guard Tower registration plus three military specialist jobs and tier-scaled shared capacity.

Current JAR SHA-256: `31c771467f7787fe8fa75ff4489aa8d87874dd5057b35f3f3d1c1f71fd514b12`.

Artifacts:
- [Self-checking release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11736788200)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11737026081)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11735878195)
- [Dedicated-server log](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11735948022)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11736267720)

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Inspect tier silhouettes and tier-scaled profession/military progression in-game, then prepare release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.
