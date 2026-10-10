#!/usr/bin/env python3
"""Validate that generated building tiers are distinct and progress in size."""

from __future__ import annotations

import gzip
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))

import generate_blueprints  # noqa: E402


def validate_family(name: str, sizes: list[tuple[int, int, int]]) -> list[str]:
    errors: list[str] = []
    prior_size: tuple[int, int, int] | None = None
    seen_payloads: set[bytes] = set()

    for level, size in enumerate(sizes, start=1):
        if len(size) != 3 or any(dimension < 1 for dimension in size):
            errors.append(f"{name} level {level}: dimensions must all be positive")
            continue

        try:
            compressed, anchor = generate_blueprints.build_structure(name, level, size)
            uncompressed = gzip.decompress(compressed)
        except (OSError, ValueError) as exc:
            errors.append(f"{name} level {level}: generation/decompression failed: {exc}")
            continue

        if uncompressed in seen_payloads:
            errors.append(f"{name} level {level}: blueprint payload duplicates an earlier tier")
        seen_payloads.add(uncompressed)

        expected_anchor = (size[0] // 2, 0, 0)
        if anchor != expected_anchor:
            errors.append(
                f"{name} level {level}: anchor offset {anchor} does not match {expected_anchor}"
            )

        if prior_size is not None:
            if any(current < previous for current, previous in zip(size, prior_size)):
                errors.append(
                    f"{name} level {level}: dimensions {size} shrink from the previous tier {prior_size}"
                )
            if not any(current > previous for current, previous in zip(size, prior_size)):
                errors.append(
                    f"{name} level {level}: dimensions {size} do not expand beyond tier {level - 1}"
                )

        prior_size = size

    if len(seen_payloads) != len(sizes):
        errors.append(f"{name}: expected {len(sizes)} unique blueprint payloads, found {len(seen_payloads)}")
    return errors


def main() -> int:
    errors = []
    errors.extend(validate_family("imperialarchive", generate_blueprints.ARCHIVE_SIZES))
    errors.extend(validate_family("imperialguardtower", generate_blueprints.TOWER_SIZES))

    if errors:
        print("Blueprint tier validation failed:")
        for error in errors:
            print(f" - {error}")
        return 1

    print("Blueprint tier validation passed: 10 distinct levels with progressive dimensions and valid anchor offsets.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
