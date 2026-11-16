"""Input validators adapted from Sword core/validators.py for Android embedded Python."""

import ipaddress
from urllib.parse import urlsplit


def validate_ip(value: str) -> str:
    try:
        return str(ipaddress.ip_address(value.strip()))
    except ValueError as exc:
        raise ValueError("Invalid IP address") from exc


def validate_subnet(value: str) -> str:
    try:
        network = ipaddress.ip_network(value.strip(), strict=False)
        if network.num_addresses > 1024:
            raise ValueError("Subnet too large for mobile scan (max /22)")
        return str(network)
    except ValueError as exc:
        raise ValueError(f"Invalid subnet: {exc}") from exc


def validate_port(value: str) -> int:
    try:
        port = int(str(value).strip())
        if not 1 <= port <= 65535:
            raise ValueError("Port out of range (1-65535)")
        return port
    except ValueError as exc:
        raise ValueError("Invalid port") from exc


def validate_url(value: str) -> str:
    raw = value.strip()
    if "://" not in raw:
        raw = "https://" + raw
    parsed = urlsplit(raw)
    if parsed.scheme not in ("http", "https"):
        raise ValueError("Invalid URL scheme (only http/https allowed)")
    if not parsed.hostname:
        raise ValueError("Invalid hostname")
    if parsed.username or parsed.password:
        raise ValueError("URL embedded credentials are not allowed")
    _ = parsed.port
    return raw


def validate_domain(value: str) -> str:
    domain = value.strip().lower()
    if not domain or len(domain) > 253 or "." not in domain:
        raise ValueError("Invalid domain")
    labels = domain.split(".")
    for label in labels:
        if not label or len(label) > 63:
            raise ValueError("Invalid domain label")
        if label.startswith("-") or label.endswith("-"):
            raise ValueError("Invalid domain hyphen placement")
        if not all(ch.isalnum() or ch == "-" for ch in label):
            raise ValueError("Invalid domain characters")
    return domain


def validate_username(value: str) -> str:
    username = value.strip()
    if not username or len(username) > 50:
        raise ValueError("Username length must be 1..50")
    if not all(ch.isalnum() or ch in "._-" for ch in username):
        raise ValueError("Username may only contain letters, digits, ., _, -")
    return username
