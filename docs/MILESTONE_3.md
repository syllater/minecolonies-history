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
- Cross-dimension commands for routes, defensive orders, governor appointment/dismissal, diplomacy and campaigns.
- Per-player map-request throttling with stale limiter cleanup and regression coverage.
- English and Dutch translation coverage checked in CI.

## Automated verification
Latest fully verified branch head `eb6b790ebf5f58ef6ea52a36ae7ba5ca33a8f4ab` passed:
- EN/NL localization validation.
- Blueprint progression validation.
- `./gradlew --no-daemon test build` and JAR artifact upload.
- SHA-256 manifest generation and `sha256sum --check SHA256SUMS.txt`.
- Headless client startup smoke test.
- Dedicated server startup and validation of all ten Structurize schematics.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38133948650

Mod JAR + `SHA256SUMS.txt` bundle: https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700525650

Artifact ZIP SHA-256: `0eb6d4c0393ffa072ad10ae711d4860161fd9565b2bca7e8cfa5f9eccb327fd3`

The checksum file inside the bundle records the JAR SHA-256, and CI verified the JAR against it before uploading.

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Inspect tier silhouettes and tier-scaled profession/military progression in-game, then prepare release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.
