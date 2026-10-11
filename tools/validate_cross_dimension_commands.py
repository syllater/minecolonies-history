#!/usr/bin/env python3
"""Guard cross-dimension strategy command registrations and localized help."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/imperium/realms/command/ImperiumCommands.java"
EN = ROOT / "src/main/resources/assets/imperium_realms/lang/en_us.json"
NL = ROOT / "src/main/resources/assets/imperium_realms/lang/nl_nl.json"

COMMANDS = {
    "defense-at": ("imperium_realms.message.defensive_orders_help",),
    "build-at": ("imperium_realms.message.supply_routes_help",),
    "resolve-at": ("imperium_realms.message.empire_petition_help",),
    "appoint-at": ("imperium_realms.message.governor_help",),
    "dismiss-at": ("imperium_realms.message.governor_help",),
    "launch-at": ("imperium_realms.message.campaign_help",),
    "improve-at": ("imperium_realms.message.diplomacy_instructions",),
}


def fail(message: str) -> None:
    print(f"CROSS-DIMENSION COMMAND VALIDATION FAIL: {message}", file=sys.stderr)
    raise SystemExit(1)


try:
    source = JAVA.read_text(encoding="utf-8")
    locales = {
        "en_us": json.loads(EN.read_text(encoding="utf-8")),
        "nl_nl": json.loads(NL.read_text(encoding="utf-8")),
    }
except (OSError, json.JSONDecodeError) as exc:
    fail(f"could not read command source or locale JSON: {exc}")

for command, help_keys in COMMANDS.items():
    if f'Commands.literal("{command}")' not in source:
        fail(f"command tree does not register {command}")
    for locale_name, locale in locales.items():
        for key in help_keys:
            value = locale.get(key)
            if not isinstance(value, str) or command not in value:
                fail(f"{locale_name} key {key} does not document {command}")

if "resolveTargetColony" not in source or "colonyByIdentity" not in source:
    fail("dimension-aware targets must resolve via the server-side MineColonies identity adapter")

if 'Commands.argument("dimension", ResourceLocationArgument.id())' not in source:
    fail("dimension arguments must use ResourceLocationArgument.id(), not Brigadier's unquoted-word parser")
if 'ResourceLocationArgument.getId(context, "dimension").toString()' not in source:
    fail("dimension argument values must be read as namespaced ResourceLocation IDs")
if 'StringArgumentType.getString(context, "dimension")' in source:
    fail("generic string parsing remains for a dimension argument and can reject namespace separators")

print("CROSS-DIMENSION COMMAND VALIDATION PASS: all seven command variants are registered and documented in EN/NL.")
