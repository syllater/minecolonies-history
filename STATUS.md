# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Active branches

- MineColonies integration: `milestone-2/colony-integration`
- Economy/policies: `milestone-3/imperial-economy`
- Parliament/politics: `milestone-4/parliament-politics`

The user authorized continued development across milestones without a separate approval pause after every milestone. Keep working through the backlog, while recording actual CI outcomes and avoiding claims that are not verified.

## Implemented so far

### MineColonies integration
- Public API adapter for server-side colony lookup.
- Stable identity based on dimension + colony ID.
- Imperium-owned SavedData for colony-linked records.
- Periodic idempotent discovery of existing/new colonies.

### Economy and policies
- Persisted treasury, tax rate, policy and tax collection day.
- Daily-unique tax calculation with policy upkeep.
- Server-side commands gated by MineColonies Manage Huts permission.
- English/Dutch commands and policy labels.
- Unit tests for economy calculation and safe ranges.

### Parliament
- Persisted proposal/voting sessions per colony.
- Constitutional Empire government label.
- One vote per colony member per proposal.
- At least one full in-game day before resolution.
- Majority rules; tied or empty vote fails.
- Three laws with economic effects and an enacted-law record.
- English/Dutch translations and unit tests.

## Verification

- Prior CI runs have passed `test build` for the integration-only code.
- CI identified and helped fix a command-tree syntax error during the parliament slice.
- Headless client smoke-test runs are being corrected: the previous failure had OpenAL no-device messages and the script waited for an audio marker, even though MineColonies/BlockUI texture atlases were loaded. The updated script uses the OpenAL null backend and checks for mod-load failure and GUI atlas readiness.
- The latest CI run for the corrected parliament branch still needs to finish before claiming this branch builds.
- No manual in-world save/reload test has been performed in this session.
