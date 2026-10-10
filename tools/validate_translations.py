#!/usr/bin/env python3
"""Validate Imperium's literal and enumerated dynamic translation keys."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_DIR = ROOT / "src" / "main" / "java"
RESOURCE_DIR = ROOT / "src" / "main" / "resources" / "assets" / "imperium_realms"
LOCALES = ("en_us", "nl_nl")

# Only full literal keys are matched here. Prefixes built with string
# concatenation are checked via DYNAMIC_SUFFIXES below.
TRANSLATION_KEY = re.compile(
    r'Component\s*\.\s*translatable\s*\(\s*"(imperium_realms\.[A-Za-z0-9_.-]*[A-Za-z0-9_-])"'
)
DYNAMIC_PREFIX = re.compile(r'"(imperium_realms\.[A-Za-z0-9_.-]+\.)"\s*\+')
GUI_KEY = re.compile(r"\$\((imperium_realms\.[A-Za-z0-9_.-]+)\)")
PLACEHOLDER = re.compile(r"%(?:[0-9]+\$)?([dsf])")

# Every value generated from an enum or persisted action ID needs a resource
# key in both locales. Add suffixes here whenever a dynamic key family grows.
DYNAMIC_SUFFIXES: dict[str, tuple[str, ...]] = {
    "imperium_realms.audit.action.": (
        "found", "province-invited", "province-joined", "province-left",
        "tax-law", "policy-law", "deposit", "withdraw", "tax-remittance",
        "unknown", "governor-appointed", "governor-dismissed",
        "separatist-petition", "petition-reassured", "petition-crackdown",
        "regional-event",
    ),
    "imperium_realms.civic_disorder.": ("calm", "strike", "revolt"),
    "imperium_realms.diplomacy.relation.": (
        "allied", "friendly", "cordial", "neutral", "unfriendly", "hostile",
    ),
    "imperium_realms.faction.": ("merchants", "commons", "nobility", "scholars"),
    "imperium_realms.military_campaign.outcome.": (
        "pending", "success", "stalemate", "defeat",
    ),
    "imperium_realms.military_campaign.type.": (
        "border_patrol", "relief_expedition", "war_campaign",
    ),
    "imperium_realms.military_posture.": ("balanced", "defensive", "offensive"),
    "imperium_realms.parliament.type.": ("tax_rate", "economic_policy"),
    "imperium_realms.province_focus.": (
        "agriculture", "trade", "scholarship", "military", "civic",
    ),
    "imperium_realms.province_tier.": (
        "settlement", "county", "duchy", "principality", "kingdom",
    ),
    "imperium_realms.regional_event.": (
        "harvest-surplus", "merchants-fair", "scholarly-exchange",
        "civic-reconciliation", "border-tensions", "winter-shortages",
        "royal-progress",
    ),
}


def load_locale(locale: str) -> dict[str, str]:
    path = RESOURCE_DIR / "lang" / f"{locale}.json"
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError(f"{path.relative_to(ROOT)} is not valid JSON: {exc}") from exc
    if not isinstance(data, dict) or not all(
        isinstance(key, str) and isinstance(value, str) for key, value in data.items()
    ):
        raise ValueError(f"{path.relative_to(ROOT)} must be a JSON object of string keys and values")
    return data


def main() -> int:
    errors: list[str] = []
    try:
        locales = {locale: load_locale(locale) for locale in LOCALES}
    except ValueError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1

    used: set[str] = set()
    observed_dynamic_prefixes: set[str] = set()
    for path in JAVA_DIR.rglob("*.java"):
        try:
            source = path.read_text(encoding="utf-8")
        except OSError as exc:
            errors.append(f"{path.relative_to(ROOT)}: cannot read source: {exc}")
            continue
        used.update(TRANSLATION_KEY.findall(source))
        observed_dynamic_prefixes.update(DYNAMIC_PREFIX.findall(source))

    for prefix in sorted(observed_dynamic_prefixes):
        suffixes = DYNAMIC_SUFFIXES.get(prefix)
        if suffixes is None:
            errors.append(f"validator: add a suffix list for dynamic translation prefix {prefix}")
            continue
        used.update(prefix + suffix for suffix in suffixes)

    gui_path = RESOURCE_DIR / "gui" / "imperial_ledger.xml"
    try:
        used.update(GUI_KEY.findall(gui_path.read_text(encoding="utf-8")))
    except OSError as exc:
        errors.append(f"{gui_path.relative_to(ROOT)}: cannot read GUI: {exc}")

    for key in sorted(used):
        values: dict[str, str] = {}
        for locale, translations in locales.items():
            value = translations.get(key)
            if value is None:
                errors.append(f"{locale}: missing translation key {key}")
            else:
                values[locale] = value

        if len(values) == len(LOCALES):
            signatures = {
                locale: sorted(PLACEHOLDER.findall(value))
                for locale, value in values.items()
            }
            if len(set(tuple(signature) for signature in signatures.values())) > 1:
                rendered = ", ".join(f"{locale}={signature}" for locale, signature in signatures.items())
                errors.append(f"{key}: placeholder mismatch ({rendered})")

    if errors:
        print("Localization validation failed:")
        for error in errors:
            print(f" - {error}")
        return 1

    print(
        f"Localization validation passed: {len(used)} literal and dynamic keys "
        "have English and Dutch translations with matching format placeholders."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
