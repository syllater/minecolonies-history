# Checkpoints

## 2026-10-09 — Parliament and Politics Slice

Branch: `milestone-4/parliament-politics`

Added:
- `GovernmentType`
- `ImperialLaw`
- `ParliamentSession`
- `ParliamentSavedData`
- `ParliamentSessionTest`
- English and Dutch parliament/law translations.
- `/imperium parliament status`
- `/imperium parliament propose public_works_act|scholarship_charter|tax_relief_charter`
- `/imperium parliament vote yes|no`
- `/imperium parliament resolve`

Design details:
- Parliament state uses Imperium-owned overworld SavedData keyed by colony dimension + ID.
- Only players recognized as members by MineColonies can vote.
- Proposing and resolving laws requires the MineColonies Manage Huts permission.
- A proposal cannot be resolved until one full in-game day after it was proposed.
- A strict majority of yes votes is required; ties and no-vote resolutions fail.
- Passed acts apply an idempotent initial economic effect and remain in the enacted-law history.

Known follow-up:
- The next slice should persist an emperor/office-holder and add political-faction support before complex unrest.
- Build and client startup must be checked in CI, then test save/reload in a real world before release claims.
