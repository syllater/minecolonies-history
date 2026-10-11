#!/usr/bin/env python3
"""Validate the release-facing mod/dependency metadata without external packages."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def fail(message: str) -> None:
    print(f"RELEASE METADATA FAIL: {message}", file=sys.stderr)
    raise SystemExit(1)


def parse_properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


props = parse_properties(ROOT / "gradle.properties")
required_properties = (
    "minecraft_version",
    "neo_version",
    "mod_id",
    "mod_version",
    "minecolonies_version",
    "structurize_version",
    "blockui_version",
    "domum_ornamentum_version",
    "multipiston_version",
)
for key in required_properties:
    if not props.get(key):
        fail(f"gradle.properties is missing {key}")

if props["minecraft_version"] != "1.21.1":
    fail(f"expected Minecraft 1.21.1, found {props['minecraft_version']}")
if props["mod_id"] != "imperium_realms":
    fail(f"unexpected mod id {props['mod_id']!r}")
version = props["mod_version"]
if not re.fullmatch(r"\d+\.\d+\.\d+(?:-[0-9A-Za-z][0-9A-Za-z.-]*)?", version):
    fail(f"mod_version is not a valid release/pre-release version: {version!r}")
if "milestone" in version.lower():
    fail(f"stale milestone build version {version!r}; use a release candidate or release version")

build = (ROOT / "build.gradle").read_text(encoding="utf-8")
metadata = (ROOT / "src/main/resources/META-INF/neoforge.mods.toml").read_text(encoding="utf-8")
for dependency, property_name in (
    ("minecolonies", "minecolonies_version"),
    ("structurize", "structurize_version"),
    ("blockui", "blockui_version"),
    ("domum-ornamentum", "domum_ornamentum_version"),
    ("multipiston", "multipiston_version"),
):
    coordinate = f"com.ldtteam:{dependency}:\u0024{{{property_name}}}"
    compile_only = f'compileOnly "{coordinate}"'
    runtime_only = f'runtimeOnly "{coordinate}"'
    if compile_only not in build:
        fail(f"missing compileOnly dependency: {coordinate}")
    if runtime_only not in build:
        fail(f"missing runtimeOnly dependency: {coordinate}")
    mod_id = dependency.replace("-", "_")
    if f'modId = "{mod_id}"' not in metadata:
        fail(f"required runtime metadata dependency missing: modId={mod_id!r}")

if 'modId = "\u0024{mod_id}"' not in metadata:
    fail("neoforge.mods.toml is missing the templated primary mod ID")
if 'version = "\u0024{mod_version}"' not in metadata:
    fail("neoforge.mods.toml is missing the templated mod version")
changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
if "Unreleased" not in changelog:
    fail("CHANGELOG.md must retain an Unreleased section")
checklist = (ROOT / "docs/PLAYTEST_RELEASE_CHECKLIST.md").read_text(encoding="utf-8")
for gate in ("actual in-world multiplayer report", "distribution license"):
    if gate.lower() not in checklist.lower():
        fail(f"release checklist is missing release gate: {gate}")

print(
    "RELEASE METADATA PASS: "
    f"mod={props['mod_id']} version={version} Minecraft={props['minecraft_version']} "
    "and all required MineColonies runtime dependencies are declared."
)
