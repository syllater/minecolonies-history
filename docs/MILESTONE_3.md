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

Full workflow: https://github.com/syllater/minecolonies-history/actions/runs/38155865389

The release candidate `0.1.0-rc1` passed:
- EN/NL translation validation.
- Generated blueprint progression validation.
- Release metadata/dependency validation.
- `./gradlew --no-daemon test build`.
- JAR SHA-256 generation/verification.
- Self-checking release ZIP build and embedded checksum validation.
- Headless client startup.
- Dedicated-server registry validation and loading all 10/10 Structurize schematics.

Artifacts:
- [Release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11707704528)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11708735039)
- [Server log](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11708552679)
- [Client log](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11708263500)

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Inspect tier silhouettes and tier-scaled profession/military progression in-game, then prepare release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.
