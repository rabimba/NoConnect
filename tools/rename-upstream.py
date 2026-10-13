#!/usr/bin/env python3
"""Rename upstream Briar/Bramble identifiers to NoConnect/Chagol.

Usage: rename.py <repo-root>    (run from anywhere; uses git mv for paths)

Idempotent: a second run changes nothing. Leaves the prebuilt Tor/lyrebird
binary Maven coordinates (org.briarproject:tor-*, lyrebird-*) untouched, and
never touches docs/, LICENSE, NOTICE or CLAUDE.md.
"""
import os
import re
import subprocess
import sys

ROOT = os.path.abspath(sys.argv[1])
REPO_URL = "https://github.com/rabimba/noconnect"

SKIP_PREFIXES = ("docs/", ".git/", ".claude/")
SKIP_FILES = {"LICENSE", "NOTICE", "CLAUDE.md"}
BINARY_EXT = {".png", ".jpg", ".jpeg", ".webp", ".gif", ".ico", ".jar", ".aar",
              ".so", ".ttf", ".otf", ".zip", ".gz", ".keystore", ".jks",
              ".bin", ".svgz", ".mp3", ".ogg", ".pdf"}

MODULES = {
    "bramble-api": "nc-api", "bramble-core": "nc-core",
    "bramble-android": "nc-android", "bramble-java": "nc-java",
    "briar-api": "noconnect-api", "briar-core": "noconnect-core",
    "briar-android": "noconnect-android", "briar-headless": "noconnect-headless",
}
# Package directory moves, most specific first
PKG_DIRS = [
    ("org/briarproject/android/dontkillmelib", "org/chagolchana/dontkillme"),
    ("org/briarproject/bramble", "org/chagolchana/chagol"),
    ("org/briarproject/briar", "org/chagolchana/noconnect"),
    ("org/briarproject/nullsafety", "org/chagolchana/nullsafety"),
    ("org/briarproject/onionwrapper", "org/chagolchana/onionwrapper"),
    ("org/briarproject/socks", "org/chagolchana/socks"),
    ("org/briarproject/util", "org/chagolchana/util"),
]

PROTECT = re.compile(r"org\.briarproject:(tor|lyrebird)-")


def sub_line(line):
    if PROTECT.search(line):
        return line
    s = line
    # Links to upstream infrastructure point at this repository instead
    s = re.sub(r"https?://code\.briarproject\.org/briar/briar(?:/[^\s\"'<>)]*)?", REPO_URL, s)
    s = re.sub(r"https?://(?:[a-z0-9-]+\.)*briarproject\.org(?:/[^\s\"'<>)]*)?", REPO_URL, s)
    # Packages (dotted and slashed forms)
    for sep in (".", "/"):
        e = re.escape(sep)
        s = re.sub(r"org" + e + r"briarproject" + e + r"android" + e + r"dontkillmelib",
                   "org" + sep + "chagolchana" + sep + "dontkillme", s)
        s = re.sub(r"org" + e + r"briarproject" + e + r"bramble", "org" + sep + "chagolchana" + sep + "chagol", s)
        s = re.sub(r"org" + e + r"briarproject" + e + r"briar", "org" + sep + "chagolchana" + sep + "noconnect", s)
        s = re.sub(r"org" + e + r"briarproject", "org" + sep + "chagolchana", s)
    for old, new in MODULES.items():
        s = re.sub(r"(?<![A-Za-z0-9_-])" + re.escape(old) + r"(?![A-Za-z0-9_])", new, s)
    s = s.replace("briar://", "noconnect://")
    s = s.replace("Bramble", "Chagol").replace("BRAMBLE", "CHAGOL").replace("bramble", "chagol")
    s = s.replace("Briar", "NoConnect").replace("BRIAR", "NOCONNECT")
    s = s.replace("briar_", "nc_")
    s = re.sub(r"briar([A-Z])", r"noConnect\1", s)
    s = s.replace("briar", "noconnect")
    return s


def tracked_files():
    out = subprocess.check_output(["git", "-C", ROOT, "ls-files", "-z"])
    return [p for p in out.decode().split("\0") if p]


def is_text_target(rel):
    if rel in SKIP_FILES or rel.startswith(SKIP_PREFIXES):
        return False
    return os.path.splitext(rel)[1].lower() not in BINARY_EXT


def git_mv(src, dst):
    os.makedirs(os.path.dirname(os.path.join(ROOT, dst)) or ROOT, exist_ok=True)
    subprocess.check_call(["git", "-C", ROOT, "mv", src, dst])


def move_paths():
    for old, new in MODULES.items():
        if os.path.isdir(os.path.join(ROOT, old)):
            git_mv(old, new)
    moved = 0
    for rel in tracked_files():
        if rel.startswith(SKIP_PREFIXES) or rel in SKIP_FILES:
            continue
        new = rel
        for a, b in PKG_DIRS:
            new = new.replace("/" + a + "/", "/" + b + "/")
        d, base = os.path.split(new)
        base2 = (base.replace("Bramble", "Chagol").replace("bramble", "chagol")
                 .replace("Briar", "NoConnect").replace("briar_", "nc_")
                 .replace("briar", "noconnect"))
        new = os.path.join(d, base2) if d else base2
        if new != rel:
            git_mv(rel, new)
            moved += 1
    # Remove now-empty old package directories
    for dp, dns, fns in os.walk(ROOT, topdown=False):
        if ".git" in dp.split(os.sep):
            continue
        if not os.listdir(dp):
            os.rmdir(dp)
    return moved


def rewrite_contents():
    changed = 0
    for rel in tracked_files():
        if not is_text_target(rel):
            continue
        p = os.path.join(ROOT, rel)
        try:
            with open(p, encoding="utf-8") as f:
                text = f.read()
        except (UnicodeDecodeError, IsADirectoryError, FileNotFoundError):
            continue
        new = "".join(sub_line(l) for l in text.splitlines(keepends=True))
        if new != text:
            with open(p, "w", encoding="utf-8") as f:
                f.write(new)
            changed += 1
    return changed


if __name__ == "__main__":
    print("paths moved:", move_paths())
    print("files rewritten:", rewrite_contents())
