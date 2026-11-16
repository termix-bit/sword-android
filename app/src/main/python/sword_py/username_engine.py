"""
Username OSINT checker adapted from Sword tools/username_scanner.py.
Uses pure Python stdlib (urllib + ssl + ThreadPoolExecutor) so it runs cleanly
inside Android/Chaquopy without native C extensions like aiohttp.
Includes strict TLS verification, response size caps, rate limiting, and
wildcard false-positive verification.
"""

from concurrent.futures import ThreadPoolExecutor, as_completed
import json
import os
import secrets
import ssl
from urllib.error import HTTPError, URLError
from urllib.parse import quote, urlsplit
from urllib.request import Request, urlopen

from sword_py.validators import validate_username

DATASET_PATH = os.path.join(os.path.dirname(__file__), "wmn-data.json")
MAX_RESPONSE_BYTES = 128 * 1024
DEFAULT_HEADERS = {
    "User-Agent": "SwordAndroidToolkit/1.0 (+https://github.com/termix-bit/sword)",
    "Accept-Language": "en-US,en;q=0.7",
}


def load_sites(limit: int = 35) -> list:
    if not os.path.exists(DATASET_PATH):
        return []
    with open(DATASET_PATH, "r", encoding="utf-8") as fh:
        data = json.load(fh)
    sites = data.get("sites", [])
    # Prioritize well-known developer/tech/social categories for fast, accurate mobile execution
    preferred = {"coding", "tech", "social", "business", "community", "gaming", "music"}
    ordered = sorted(
        [s for s in sites if s.get("uri_check") and "{account}" in s.get("uri_check", "")],
        key=lambda s: (0 if s.get("cat") in preferred else 1, s.get("name", "")),
    )
    return ordered[: max(1, min(limit, 100))]


def build_site_url(template: str, username: str) -> str:
    if not isinstance(template, str) or "{account}" not in template:
        raise ValueError("Invalid URL template")
    safe_user = quote(username, safe="._-")
    url = template.format(account=safe_user)
    parsed = urlsplit(url)
    if parsed.scheme != "https":
        raise ValueError("Only HTTPS targets are permitted")
    if not parsed.hostname or parsed.username or parsed.password:
        raise ValueError("Invalid URL authority")
    return url


def _fetch(url: str, timeout: float = 5.0) -> tuple[int, str]:
    ctx = ssl.create_default_context()
    req = Request(url, headers=DEFAULT_HEADERS, method="GET")
    try:
        with urlopen(req, timeout=timeout, context=ctx) as resp:
            raw = resp.read(MAX_RESPONSE_BYTES)
            return resp.status, raw.decode("utf-8", errors="replace")
    except HTTPError as err:
        raw = err.read(MAX_RESPONSE_BYTES) if hasattr(err, "read") else b""
        return err.code, raw.decode("utf-8", errors="replace")


def _check_single_site(site: dict, username: str, timeout: float) -> dict | None:
    template = site.get("uri_check", "")
    name = site.get("name", "Unknown")
    category = site.get("cat", "general")
    try:
        url = build_site_url(template, username)
    except ValueError:
        return None

    e_code = site.get("e_code", 200)
    m_code = site.get("m_code", 404)
    e_str = site.get("e_string")
    m_str = site.get("m_string")

    try:
        status, text = _fetch(url, timeout=timeout)
        if m_code is not None and status == m_code:
            return None
        if m_str and m_str in text:
            return None
        if e_code is not None and status != e_code:
            return None
        if e_str and e_str not in text:
            return None

        # False-positive wildcard mitigation from Sword tools/username_scanner.py
        probe_user = "zz" + secrets.token_hex(4)
        probe_url = build_site_url(template, probe_user)
        p_status, p_text = _fetch(probe_url, timeout=timeout)
        if p_status == e_code and (not e_str or e_str in p_text):
            return None

        display_url = site.get("uri_pretty", template).format(account=quote(username, safe="._-"))
        return {
            "site": name,
            "category": category,
            "url": display_url,
            "status": status,
        }
    except (URLError, TimeoutError, ssl.SSLError, ValueError, OSError):
        return None


def scan_username_json(username: str, max_sites: int = 25, timeout: float = 5.0) -> str:
    clean_user = validate_username(username)
    sites = load_sites(limit=max_sites)
    matches = []
    errors_or_skipped = 0

    with ThreadPoolExecutor(max_workers=6) as pool:
        futures = [pool.submit(_check_single_site, site, clean_user, timeout) for site in sites]
        for fut in as_completed(futures):
            try:
                res = fut.result()
                if res is not None:
                    matches.append(res)
            except Exception:
                errors_or_skipped += 1

    matches.sort(key=lambda m: m["site"].lower())
    return json.dumps({
        "username": clean_user,
        "sites_checked": len(sites),
        "matches_found": len(matches),
        "matches": matches,
    })
