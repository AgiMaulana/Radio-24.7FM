---
name: respond-to-pr-reviews
description: "Handle PR review comments end-to-end: triage, fix or argue, reply, and push."
---

# Respond to PR Reviews

Handle GitHub PR review comments end-to-end: fetch, triage, fix or argue, reply, amend, and push.

### Trigger phrases

- "fetch reviews", "respond to PR comments"
- "address review feedback", "reply to reviewer"
- "handle PR reviews"

## Prerequisites

- GitHub MCP server configured, OR `.env` with `GITHUB_PERSONAL_ACCESS_TOKEN`
- Current branch is the PR branch
- PR number known (find via `gh pr view` if not given)

## Workflow

### 1. Fetch review comments

Resolve owner and repo if not known:
```bash
gh repo view --json owner,name -q '.owner.login + "/" + .name'
```

**Via MCP:**
```
mcp__github__pull_request_read(method="get_review_comments", owner="AgiMaulana", repo="Radio-24.7FM", pullNumber=<pr_number>)
```

**Fallback via gh CLI:**
```bash
gh api repos/AgiMaulana/Radio-24.7FM/pulls/<pr_number>/comments
```

### 2. Triage each comment

For every comment, read the relevant code section, then decide:

**Agree** if the suggestion:
- Fixes a real bug or crash risk
- Improves correctness, safety, or maintainability
- Addresses a legitimate style/pattern issue

**Argue** if the suggestion:
- Is already addressed in the current code (stale comment)
- Conflicts with an intentional design decision
- Adds complexity without clear benefit
- Is out of scope for the PR

When in doubt, agree — reviewer context is valuable.

### 3. Apply fixes (agreed comments)

Edit the code to implement the suggestion. Keep changes minimal — don't refactor beyond what the comment asks for.

### 4. Post replies

**Via gh CLI:**
```bash
gh api "repos/OWNER/REPO/pulls/PR_NUM/comments/COMMENT_ID/replies" --method POST --field body="REPLY_BODY"
```

Example:
```bash
gh api "repos/AgiMaulana/Radio-24.7FM/pulls/23/comments/3142356051/replies" --method POST --field body="👍 Good catch"
```

Reply format:
- **Agreed**: `"👍 [One sentence: what was fixed and why the reviewer was right.]"`
- **Argued**: `"👀 [Technical counter-argument. Reference code, constraints, or design decisions.]"`

### 5. Verify and amend

Run lint before amending:
```bash

```

Then amend and force push:
```bash
git add <changed files>
git commit --amend --no-edit
git push --force-with-lease origin <branch>
```

## Reply tone guidelines

- Be concise: 1-3 sentences per reply
- When arguing: be direct but collegial; acknowledge the reviewer's intent
- When agreeing: don't over-explain; state what changed and why it's better
- Never say "per your suggestion" or other filler phrases

## Example replies

**Agreed:**
> 👍 Good catch. Wrapped in `remember(source.url, source.headers)` so the request only rebuilds when inputs change.

**Argued:**
> 👀 Respectfully disagree — this is already handled: `getLoader()` delegates to the app-level singleton. No separate cache exists.
