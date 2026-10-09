#!/usr/bin/env python3
"""Generate original Structurize blueprints for Imperium's MineColonies huts.

The blueprint NBT is authored from a small, explicit block palette rather than
copying third-party structure assets. Python's standard library is sufficient.
"""
from __future__ import annotations

import gzip
import json
import struct
import sys
import zlib
from pathlib import Path
from typing import Iterable


MC_DATA_VERSION = 3953  # Minecraft Java Edition 1.21.1
PACK_NAME = "imperium_european"
MOD_ID = "imperium_realms"

# Palette index 0 is air, as expected by Structurize's Blueprint implementation.
PALETTE: list[tuple[str, dict[str, str]]] = [
    ("minecraft:air", {}),
    ("minecraft:stone_bricks", {}),
    ("minecraft:cobblestone", {}),
    ("minecraft:oak_planks", {}),
    ("minecraft:spruce_planks", {}),
    ("minecraft:bricks", {}),
    ("minecraft:glass", {}),
    ("minecraft:polished_deepslate", {}),
    ("minecraft:bookshelf", {}),
    ("imperium_realms:blockhutimperialarchive", {"facing": "north"}),
    ("imperium_realms:blockhutimperialguardtower", {"facing": "north"}),
]

ARCHIVE_SIZES = [(7, 5, 7), (7, 6, 7), (9, 7, 9), (9, 8, 9), (11, 9, 11)]
TOWER_SIZES = [(7, 5, 7), (7, 7, 7), (9, 8, 9), (9, 9, 9), (11, 11, 11)]


def encode_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    if len(encoded) > 65535:
        raise ValueError("NBT strings cannot exceed 65535 bytes")
    return struct.pack(">H", len(encoded)) + encoded


def tag(tag_type: int, name: str, payload: bytes) -> bytes:
    return bytes((tag_type,)) + encode_string(name) + payload


def string_payload(value: str) -> bytes:
    return encode_string(value)


def compound_payload(children: Iterable[bytes]) -> bytes:
    return b"".join(children) + b"\x00"


def compound_tag(name: str, children: Iterable[bytes]) -> bytes:
    return tag(10, name, compound_payload(children))


def list_tag(name: str, element_type: int, payloads: Iterable[bytes]) -> bytes:
    values = list(payloads)
    body = bytes((element_type,)) + struct.pack(">i", len(values)) + b"".join(values)
    return tag(9, name, body)


def int_array_tag(name: str, values: list[int]) -> bytes:
    return tag(11, name, struct.pack(">i", len(values)) + b"".join(struct.pack(">i", value) for value in values))


def palette_entry(block_name: str, properties: dict[str, str]) -> bytes:
    fields = [tag(8, "Name", string_payload(block_name))]
    if properties:
        fields.append(compound_tag(
            "Properties",
            [tag(8, key, string_payload(value)) for key, value in sorted(properties.items())],
        ))
    return compound_payload(fields)


def pack_indices(layers: list[list[list[int]]], size_x: int, size_y: int, size_z: int) -> list[int]:
    flattened = [
        layers[y][z][x]
        for y in range(size_y)
        for z in range(size_z)
        for x in range(size_x)
    ]
    packed: list[int] = []
    for index in range(0, len(flattened), 2):
        upper = flattened[index] & 0xFFFF
        lower = (flattened[index + 1] & 0xFFFF) if index + 1 < len(flattened) else 0
        value = (upper << 16) | lower
        if value >= 0x80000000:
            value -= 0x100000000
        packed.append(value)
    return packed


def add_block(layers: list[list[list[int]]], x: int, y: int, z: int, value: int) -> None:
    if 0 <= y < len(layers) and 0 <= z < len(layers[y]) and 0 <= x < len(layers[y][z]):
        layers[y][z][x] = value


def build_structure(kind: str, level: int, dimensions: tuple[int, int, int]) -> tuple[bytes, tuple[int, int, int]]:
    size_x, size_y, size_z = dimensions
    blocks = [[[0 for _ in range(size_x)] for _ in range(size_z)] for _ in range(size_y)]
    center_x = size_x // 2
    center_z = size_z // 2
    wall_top = size_y - 3
    is_tower = kind == "imperialguardtower"
    anchor_index = 10 if is_tower else 9

    # A full foundation with a centered MineColonies hut anchor at the front.
    for z in range(size_z):
        for x in range(size_x):
            on_edge = x in (0, size_x - 1) or z in (0, size_z - 1)
            add_block(blocks, x, 0, z, 1 if on_edge else (4 if is_tower else 3))
    add_block(blocks, center_x, 0, 0, anchor_index)

    # Original medieval walling: masonry corners, timber panels and high windows.
    for y in range(1, wall_top + 1):
        for z in range(size_z):
            for x in range(size_x):
                on_edge = x in (0, size_x - 1) or z in (0, size_z - 1)
                if not on_edge:
                    continue

                # Two-block-tall, open entry above the anchor block.
                if z == 0 and x == center_x and y <= 2:
                    continue

                corner = x in (0, size_x - 1) and z in (0, size_z - 1)
                if y == 2 and not corner:
                    if z in (0, size_z - 1) and x in (1, size_x - 2):
                        add_block(blocks, x, y, z, 6)
                        continue
                    if x in (0, size_x - 1) and z == center_z:
                        add_block(blocks, x, y, z, 6)
                        continue

                if corner or y == wall_top:
                    value = 1 if (level % 2 == 1 or is_tower) else 5
                else:
                    value = 4 if (is_tower and (x + z + level) % 3 == 0) else 3
                add_block(blocks, x, y, z, value)

    # A compact interior focal point: shelving for the Archive, armory masonry
    # for the tower. This intentionally uses ordinary full blocks (no block-entity
    # NBT is required) so builders can place every tier in an existing colony.
    back_z = size_z - 2
    if not is_tower:
        add_block(blocks, center_x, 1, back_z, 8)
        if size_x >= 9:
            add_block(blocks, center_x - 1, 1, back_z, 8)
            add_block(blocks, center_x + 1, 1, back_z, 8)
    else:
        add_block(blocks, center_x - 1, 1, back_z, 2)
        add_block(blocks, center_x + 1, 1, back_z, 2)

    # A layered, flat-roofed medieval crown with a recessed upper parapet.
    roof_material = 7 if is_tower else (5 if level >= 3 else 1)
    roof_base_y = size_y - 2
    for z in range(size_z):
        for x in range(size_x):
            # The lower roof course is a complete weatherproof roof deck.
            add_block(blocks, x, roof_base_y, z, roof_material)

    # The top level is an open roof terrace surrounded by a parapet. Alternating
    # crenellations give the guard tower a distinct silhouette.
    top_y = size_y - 1
    for z in range(1, size_z - 1):
        for x in range(1, size_x - 1):
            edge = x in (1, size_x - 2) or z in (1, size_z - 2)
            if not edge:
                continue
            crenellation = is_tower and ((x + z + level) % 2 == 0)
            if is_tower and not crenellation:
                continue
            add_block(blocks, x, top_y, z, roof_material if not is_tower else 1)

    # Add the front/back outer edges to the parapet, making its silhouette clear.
    for x in range(size_x):
        add_block(blocks, x, top_y, 1, roof_material)
        add_block(blocks, x, top_y, size_z - 2, roof_material)
    for z in range(size_z):
        add_block(blocks, 1, top_y, z, roof_material)
        add_block(blocks, size_x - 2, top_y, z, roof_material)

    # Restore the hut anchor if any later decorative pass ever overlaps it.
    add_block(blocks, center_x, 0, 0, anchor_index)

    palette_payloads = [palette_entry(block, properties) for block, properties in PALETTE]
    size_x_tag = tag(2, "size_x", struct.pack(">h", size_x))
    size_y_tag = tag(2, "size_y", struct.pack(">h", size_y))
    size_z_tag = tag(2, "size_z", struct.pack(">h", size_z))
    palette_tag = list_tag("palette", 10, palette_payloads)
    block_indices_tag = int_array_tag("blocks", pack_indices(blocks, size_x, size_y, size_z))
    empty_compound_list_tile_entities = list_tag("tile_entities", 10, [])
    empty_compound_list_entities = list_tag("entities", 10, [])
    required_mods_tag = list_tag(
        "required_mods", 8,
        [string_payload("minecraft"), string_payload("minecolonies"), string_payload(MOD_ID)],
    )
    name_tag = tag(8, "name", string_payload(f"{kind} level {level}"))
    mcversion_tag = tag(3, "mcversion", struct.pack(">i", MC_DATA_VERSION))
    version_tag = tag(1, "version", b"\x01")

    primary_offset = compound_tag("primary_offset", [
        tag(3, "x", struct.pack(">i", center_x)),
        tag(3, "y", struct.pack(">i", 0)),
        tag(3, "z", struct.pack(">i", 0)),
    ])
    structurize_optional = compound_tag("structurize", [primary_offset])
    optional_data_tag = compound_tag("optional_data", [structurize_optional])

    root = tag(10, "", compound_payload([
        version_tag,
        size_x_tag,
        size_y_tag,
        size_z_tag,
        palette_tag,
        block_indices_tag,
        empty_compound_list_tile_entities,
        empty_compound_list_entities,
        required_mods_tag,
        name_tag,
        mcversion_tag,
        optional_data_tag,
    ]))

    # Sanity checks before the build can package a broken structure.
    decompressed = gzip.decompress(gzip.compress(root, mtime=0))
    if not decompressed.startswith(b"\x0a\x00\x00"):
        raise ValueError(f"{kind}{level}: invalid NBT root")
    for required in (b"palette", b"blocks", b"tile_entities", b"required_mods", b"primary_offset"):
        if required not in decompressed:
            raise ValueError(f"{kind}{level}: missing NBT field {required!r}")
    if f"imperium_realms:blockhut{'imperialguardtower' if is_tower else 'imperialarchive'}".encode() not in decompressed:
        raise ValueError(f"{kind}{level}: missing hut anchor in palette")

    # Verify every stored palette index is valid; the dimensions and serialized
    # short-index stream are generated from the same in-memory grid.
    if any(value < 0 or value >= len(PALETTE) for layer in blocks for row in layer for value in row):
        raise ValueError(f"{kind}{level}: block palette index out of range")
    if blocks[0][0][center_x] != anchor_index:
        raise ValueError(f"{kind}{level}: anchor missing at primary offset")
    return gzip.compress(root, mtime=0), (center_x, 0, 0)


def write_png(path: Path) -> None:
    """Write a tiny original 32x32 shield icon using only the standard library."""
    width = height = 32
    rows = []
    for y in range(height):
        row = bytearray((0,))
        for x in range(width):
            if 5 <= x <= 26 and 4 <= y <= 24 and abs(x - 16) <= (15 - y // 2):
                if y < 9 or x in (5, 6, 25, 26):
                    rgba = (83, 60, 38, 255)
                elif x in (14, 15, 16, 17) or y in (14, 15):
                    rgba = (230, 194, 110, 255)
                else:
                    rgba = (120, 36, 33, 255)
            else:
                rgba = (0, 0, 0, 0)
            row.extend(rgba)
        rows.append(bytes(row))

    def chunk(kind: bytes, payload: bytes) -> bytes:
        return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", zlib.crc32(kind + payload) & 0xFFFFFFFF)

    raw = b"".join(rows)
    png = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    path.write_bytes(png)


def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: generate_blueprints.py <output-resource-root> <unused>", file=sys.stderr)
        # The second argument is intentionally kept for Gradle task compatibility.
        if len(sys.argv) < 2:
            return 2
    output_root = Path(sys.argv[1])
    pack_root = output_root / "blueprints" / MOD_ID / PACK_NAME
    archive_dir = pack_root / "buildings" / "imperial_archive"
    tower_dir = pack_root / "buildings" / "imperial_guard_tower"
    archive_dir.mkdir(parents=True, exist_ok=True)
    tower_dir.mkdir(parents=True, exist_ok=True)

    pack_json = {
        "name": PACK_NAME,
        "icon": "icon.png",
        "authors": ["Imperium: European Realms"],
        "desc": "Original medieval European-inspired administrative and military buildings for Imperium.",
        "mods": ["minecraft", "minecolonies", MOD_ID],
        "version": 1.0,
        "pack-format": 1,
    }
    (pack_root / "pack.json").write_text(json.dumps(pack_json, indent=2) + "\n", encoding="utf-8")
    write_png(pack_root / "icon.png")

    created = 0
    for level, size in enumerate(ARCHIVE_SIZES, start=1):
        payload, _ = build_structure("imperialarchive", level, size)
        target = archive_dir / f"imperialarchive{level}.blueprint"
        target.write_bytes(payload)
        created += 1

    for level, size in enumerate(TOWER_SIZES, start=1):
        payload, _ = build_structure("imperialguardtower", level, size)
        target = tower_dir / f"imperialguardtower{level}.blueprint"
        target.write_bytes(payload)
        created += 1

    print(f"Generated and validated {created} original Structurize blueprints in {pack_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
