#!/usr/bin/env python3
"""Validate Imperium's literal translation keys and format placeholders."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_DIR = ROOT / "src" / "main" / "java"
RESOURCE_DIR = ROOT / "src" / "main" / "resources" / "assets" / "imperium_realms"
LOCALES = ("en_us", "nl_nl")
TRANSLATION_KEY = re.compile(
    r'Component\s*\.\s*translatable\s*\(\s*"((?:imperium_realms)\.[A-Za-z0-9_.-]*[A-Za-z0-9_-])"'
)
GUI_KEY = re.compile(r"\$\((imperium_realms\.[A-Za-z0-9_.-]+)\)")
PLACEHOLDER = re.compile(r"%(?:[0-9]+\$)?([dsf])")


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
    for path in JAVA_DIR.rglob("*.java"):
        try:
            source = path.read_text(encoding="utf-8")
        except OSError as exc:
            errors.append(f"{path.relative_to(ROOT)}: cannot read source: {exc}")
            continue
        used.update(TRANSLATION_KEY.findall(source))

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
        f"Localization validation passed: {len(used)} literal keys have English and Dutch "
        "translations with matching format placeholders."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
