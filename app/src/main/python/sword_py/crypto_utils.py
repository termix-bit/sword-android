"""Cryptographic & encoding utilities adapted from Sword modules/crypto for Python bridge verification."""

import base64
import hashlib
import hmac
import json


def compute_hashes(text: str) -> str:
    raw = text.encode("utf-8")
    return json.dumps({
        "sha256": hashlib.sha256(raw).hexdigest(),
        "sha512": hashlib.sha512(raw).hexdigest(),
        "sha1_legacy": hashlib.sha1(raw).hexdigest(),
        "md5_legacy": hashlib.md5(raw).hexdigest(),
    })


def verify_hash_constant_time(text: str, expected_hex: str, algorithm: str = "sha256") -> bool:
    algo = algorithm.lower().strip()
    raw = text.encode("utf-8")
    if algo == "sha256":
        actual = hashlib.sha256(raw).hexdigest()
    elif algo == "sha512":
        actual = hashlib.sha512(raw).hexdigest()
    else:
        raise ValueError(f"Unsupported algorithm: {algorithm}")
    return hmac.compare_digest(actual.lower(), expected_hex.strip().lower())


def rot13(text: str) -> str:
    out = []
    for ch in text:
        if "a" <= ch <= "z":
            out.append(chr((ord(ch) - ord("a") + 13) % 26 + ord("a")))
        elif "A" <= ch <= "Z":
            out.append(chr((ord(ch) - ord("A") + 13) % 26 + ord("A")))
        else:
            out.append(ch)
    return "".join(out)


def b64_encode(text: str) -> str:
    return base64.b64encode(text.encode("utf-8")).decode("ascii")


def b64_decode(encoded: str) -> str:
    return base64.b64decode(encoded.encode("ascii"), validate=True).decode("utf-8")
