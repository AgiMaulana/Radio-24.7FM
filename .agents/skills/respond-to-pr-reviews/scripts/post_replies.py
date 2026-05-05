#!/usr/bin/env python3
"""Post replies to PR review comments.

Usage:
    python3 post_replies.py <owner> <repo> <pr_number> <replies_json>

replies_json: JSON array of {"comment_id": int, "body": str}
"""

import json
import ssl
import subprocess
import sys
import urllib.request
from pathlib import Path

# Ignore SSL verification issues on some systems (e.g. macOS)
ssl._create_default_https_context = ssl._create_unverified_context


def read_token() -> str:
    env_path = Path(".env")
    if env_path.exists():
        env = dict(
            line.split("=", 1)
            for line in env_path.read_text().splitlines()
            if "=" in line and not line.startswith("#")
        )
        token = env.get("GITHUB_PERSONAL_ACCESS_TOKEN", "").strip()
        if token:
            return token
    return subprocess.check_output(["gh", "auth", "token"], text=True).strip()


def main() -> None:
    token = read_token()
    owner, repo, pr_number = sys.argv[1], sys.argv[2], sys.argv[3]
    replies = json.loads(sys.argv[4])

    for reply in replies:
        url = (
            f"https://api.github.com/repos/{owner}/{repo}"
            f"/pulls/{pr_number}/comments/{reply['comment_id']}/replies"
        )
        data = json.dumps({"body": reply["body"]}).encode()
        request = urllib.request.Request(
            url,
            data=data,
            method="POST",
            headers={
                "Authorization": f"token {token}",
                "Accept": "application/vnd.github.v3+json",
                "Content-Type": "application/json",
            },
        )
        status = urllib.request.urlopen(request).status
        print(f"[{status}] comment {reply['comment_id']}: {reply['body'][:60]}")


if __name__ == "__main__":
    main()
