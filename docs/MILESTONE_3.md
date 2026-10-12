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

Full green workflow: [38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437) on source commit `e3e4e7c541ffc0de78daf1a3d8b3483db28d2b22`.

All four CI jobs passed:
- EN/NL localization (298 keys), generated tier progression, dependency/release metadata, cross-dimension command/help and BlockUI ledger wiring (40 buttons/38 handlers/2 dedicated actions).
- `./gradlew --no-daemon test build`, JAR SHA-256 generation/verification, and self-checking RC ZIP assembly.
- Headless client startup reached `Sound engine started`.
- Dedicated server validated `jobs=6 buildings=2 guardTypes=3 modules=6` and loaded all `10/10` Structurize schematics.
- Five live GameTests passed: daily tax/economy/realm persistence; discovery and serialization of a real MineColonies colony; real Archive registration plus the three citizen jobs and tier-scaled hiring; and real Guard Tower registration plus the three military specialist jobs and tier-scaled shared capacity.

Current JAR SHA-256: `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`.

Artifacts:
- [Self-checking release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763166421)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762818347)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763480400)
- [Dedicated-server log](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762294179)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762678784)

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Inspect tier silhouettes and tier-scaled profession/military progression in-game, then prepare release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.


## Latest reproducible package (2026-10-12)

The latest full green verification is [workflow 38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437) on commit `e3e4e7c541ffc0de78daf1a3d8b3483db28d2b22`. All four jobs passed. The candidate JAR SHA-256 is `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`. The current candidate bundle is [here](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763166421), with the [JAR/checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762818347).


## Fifth integration regression test

The green [workflow 38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437) adds a live Minecraft command test. Through the actual Brigadier dispatcher it verifies that the colony owner can submit a tax bill, while an unauthorised non-owner is denied tax and policy mutations without changing proposal state. The current candidate JAR SHA-256 is `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`. Full survival build/upgrade, saved-world reload, GUI and two-player tests remain manual acceptance requirements.
