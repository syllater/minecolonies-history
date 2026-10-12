#!/usr/bin/env python3
"""Validate that every BlockUI ledger control is wired to a registered command."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GUI_XML = ROOT / "src/main/resources/assets/imperium_realms/gui/imperial_ledger.xml"
WINDOW = ROOT / "src/main/java/com/imperium/realms/client/ImperiumLedgerWindow.java"
COMMAND_SOURCES = (
    ROOT / "src/main/java/com/imperium/realms/command/ImperiumCommands.java",
    ROOT / "src/main/java/com/imperium/realms/command/ImperiumCrossDimensionRealmCommands.java",
)
ENTRY_PATTERN = re.compile(r'Map\.entry\("([^"]+)",\s*"([^"]+)"\)')
LITERAL_PATTERN = re.compile(r'Commands\.literal\("([^"]+)"\)')
DEDICATED_HANDLERS = {"close", "empire_map"}
# These are terminal argument values for GUI actions. Every parent command
# path is still required to be a registered literal in MineColonies' tree.
ARGUMENT_TERMINALS = {
    "0", "5", "10", "15", "20", "25", "50", "100",
    "balanced", "mercantile", "welfare", "austerity",
    "agriculture", "trade", "scholarship", "military", "civic",
    "defensive", "offensive",
}


def fail(message: str) -> None:
    print(f"LEDGER CONTROL VALIDATION FAIL: {message}", file=sys.stderr)
    raise SystemExit(1)


try:
    root = ET.parse(GUI_XML).getroot()
    window_source = WINDOW.read_text(encoding="utf-8")
    command_source = "\n".join(path.read_text(encoding="utf-8") for path in COMMAND_SOURCES)
except (OSError, ET.ParseError) as exc:
    fail(f"could not read BlockUI XML or command sources: {exc}")

button_ids = [
    element.attrib["id"]
    for element in root.iter("button")
    if element.attrib.get("id")
]
if not button_ids:
    fail("ledger XML contains no buttons")
if len(button_ids) != len(set(button_ids)):
    fail("ledger XML contains duplicate button IDs")

entries = ENTRY_PATTERN.findall(window_source)
if not entries:
    fail("ImperiumLedgerWindow has no COMMANDS Map.entry handlers")
handler_ids = [entry_id for entry_id, _ in entries]
if len(handler_ids) != len(set(handler_ids)):
    fail("ImperiumLedgerWindow has duplicate handler IDs")

button_set = set(button_ids)
handler_set = set(handler_ids)
unwired = sorted(button_set - handler_set - DEDICATED_HANDLERS)
unused = sorted(handler_set - button_set)
if unwired:
    fail(f"buttons have no handler: {', '.join(unwired)}")
if unused:
    fail(f"handlers have no corresponding XML button: {', '.join(unused)}")

literal_nodes = set(LITERAL_PATTERN.findall(command_source))
if "imperium" not in literal_nodes:
    fail("server command sources do not register the imperium root")

for button_id, command in entries:
    tokens = command.split()
    if not tokens or tokens[0] != "imperium":
        fail(f"{button_id}: handler must issue a command under /imperium, found {command!r}")
    path_tokens = tokens[1:]
    if not path_tokens:
        fail(f"{button_id}: command has no subcommand")
    for token in path_tokens[:-1]:
        if token not in literal_nodes:
            fail(f"{button_id}: command path {command!r} uses unregistered intermediate literal {token!r}")
    terminal = path_tokens[-1]
    if terminal not in ARGUMENT_TERMINALS and terminal not in literal_nodes:
        fail(f"{button_id}: command path {command!r} uses unregistered terminal literal {terminal!r}")

print(
    "LEDGER CONTROL VALIDATION PASS: "
    f"{len(button_ids)} BlockUI buttons, {len(handler_ids)} command handlers, "
    f"{len(DEDICATED_HANDLERS)} dedicated actions; all UI command paths are registered."
)
