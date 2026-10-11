# Milestone 3 — First Playable Vertical Slice

## Delivered systems
- Real MineColonies colonies serve as provinces; custom state is keyed by dimension + colony ID.
- Imperial Archive and Imperial Guard Tower with levels 1–5 and ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat professions plus specialist guard roles.
- Treasury, taxation, investment, economic policy, province focus/development and server-validated commands.
- Parliament proposals with faction votes, Emperor assent/veto and shared realm laws.
- Persistent realm membership, invitations, central treasury, audit entries, supply routes, defensive orders and governors.
- Up to three concurrent military operations with postures, specialist training and deterministic priority actions.
- BlockUI Imperial Ledger with shortcuts for politics, economy, development and realm systems.
- Chat-based strategic overview plus graphical Imperial Theatre with server-authoritative province snapshots, pan/zoom/recenter and province selection.
- English and Dutch translation coverage checked in CI.

## Automated verification
Commit `bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73` passed:
- EN/NL localization validation.
- Blueprint progression validation.
- `./gradlew --no-daemon test build` and JAR artifact upload.
- Headless client startup smoke test.
- Dedicated server startup and validation of all ten Structurize schematics.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38119451312

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Richer visual differentiation between building levels, release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.
