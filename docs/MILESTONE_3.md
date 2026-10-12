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

Full green workflow: [38287296842](https://github.com/syllater/minecolonies-history/actions/runs/38287296842) on source commit `8d75647dd41f2527976f142a1361978a7619282b`.

All four CI jobs passed:
- EN/NL localization, tier progression, dependency/release metadata, cross-dimension command/help and BlockUI ledger wiring.
- `./gradlew --no-daemon test build`, JAR SHA-256 verification and self-checking RC ZIP assembly.
- Headless client startup reached `Sound engine started`.
- Dedicated server validated `jobs=6 buildings=2 guardTypes=3 modules=6` and loaded all `10/10` Structurize schematics.
- Five live GameTests passed: daily tax/economy/realm persistence; real colony discovery/SavedData round-trip; Archive registration/jobs/capacity; Guard Tower specialists/capacity; and owner/non-owner tax/policy/deposit/withdraw authorization with denied-action state immutability.

Current JAR SHA-256: `a7aa3b3bf6fde8d62eecb6b26079cf694e23f3913e924e8906dbaa137002aee4`.

Artifacts:
- [Self-checking release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11764435629)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11763264551)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11763737489)
- [Dedicated-server log](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11763947174)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11763882201)

## Required before calling the project release-ready
- A real in-world test of hut placement, hiring, levels 1–5 and save/reload.
- A click-through playtest of the Imperial Theatre with a real realm and multiple provinces.
- A dedicated-server two-player test for membership, Emperor permissions, treasury concurrency and reconnects.
- Inspect tier silhouettes and tier-scaled profession/military progression in-game, then prepare release notes and final package review.
- An explicit decision on the project's distribution license; do not publish a final package until it is decided.




## Latest verified treasury authorization — 2026-10-12

The full green [workflow 38287296842](https://github.com/syllater/minecolonies-history/actions/runs/38287296842) on `8d75647dd41f2527976f142a1361978a7619282b` includes a live command integration test. The colony owner can submit a tax proposal and perform treasury deposit/withdraw; an unauthorised non-owner cannot execute tax, policy, deposit or withdrawal commands, and rejected actions leave proposals and balances unchanged. The test checks exact transfer deltas against the actual starting treasury balance.

- JAR SHA-256: `a7aa3b3bf6fde8d62eecb6b26079cf694e23f3913e924e8906dbaa137002aee4`
- [Release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11764435629)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11763264551)
- [GameTest output](https://github.com/syllater/minecolonies-history/actions/runs/38287296842/artifacts/11763737489)
- Survival build/upgrades, sustained worker behavior/save-reload, GUI click-through and true two-player concurrency/reconnect still require manual acceptance; balance and license decisions remain open.
