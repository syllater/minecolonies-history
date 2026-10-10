# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- This connected environment edits through GitHub APIs and uses Actions for build/runtime checks; it cannot execute a local shell. Build/runtime claims below refer to actual Actions results.

## Latest fully verified commit
Commit `66465929e935ac1bff8fe846c8ee31f284dad3b6` passed the complete workflow:
- Literal English/Dutch localization validation: success.
- Gradle compilation, unit tests and mod JAR packaging: success.
- Headless Minecraft client startup smoke test: success.
- Dedicated server startup and validation of all 10 Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075075711

Earlier verified test coverage includes strategic map grid cases and NBT round-trip tests for provincial economy/profession progress and empire membership/laws/routes/defence/governors/audit state.

## Next prepared change
The next bundled commit expands the translation validator to cover all known dynamic suffix families and adds the missing `imperium_realms.audit.action.regional-event` string in English and Dutch. That expanded validator has not yet had its own CI run.

## Major systems implemented
- MineColonies API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat MineColonies professions with work AI.
- Imperial Siege Engineer, Field Medic and Cavalier specialist training tracks.
- Daily taxation, treasury, knowledge investment, economic policies, faction voting and parliamentary decisions.
- Citizen-happiness feedback, faction approval, legitimacy, stability, unrest, strikes and revolts.
- Diplomatic relations and persisted strategic military campaigns.
- Province ranks and agriculture, trade, scholarship, military and civic focus.
- Federated realms with Emperor/capital, up to 64 real colony provinces, invitations and shared central reserve.
- Imperial tax/policy laws voted through the capital propagate to current provinces; new members inherit active laws.
- Enacted imperial tax law remits a portion of already-collected provincial receipts to the central reserve, avoiding a second citizen tax.
- Persistent audit history, governor appointments, regional events, cohesion and separatist petitions.
- Up to three concurrent operations against distinct targets, persisted doctrine, direct supply routes and temporary emergency defence orders.
- Strategic priority planner and BlockUI controls for doctrine, priority route construction and priority defence.
- `/imperium empire map` renders a coordinate grid from loaded MineColonies colony centers, listing other-dimension/unresolved colonies separately.
- English and Dutch translations, literal-key validation in CI, plus expanded dynamic-key validation being added.
- CI for build/tests, dedicated-server schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, then save/reload an existing and a new MineColonies colony.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels and deeper building modules.
- The coordinate map is a compact text-grid/chat view, not a fully interactive pan/zoom theatre map.
- Release notes, final artifact/package review and an explicit distribution/license decision.

## Next actions
1. Run CI for the dynamic translation-key family validator and fix genuine missing keys/placeholders.
2. Continue improving high-value runtime/persistence features; defer non-blocking cosmetic warnings.
3. Keep survival and multiplayer tests marked pending until actually performed in Minecraft.
