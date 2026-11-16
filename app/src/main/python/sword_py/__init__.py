"""Sword Android Embedded Python Engine (pure stdlib, Android-safe)."""

import sys
import platform

def runtime_info() -> dict:
    return {
        "python_version": sys.version.split()[0],
        "platform": platform.platform(),
        "engine": "Chaquopy Embedded Python",
        "safe_mode": True,
    }
