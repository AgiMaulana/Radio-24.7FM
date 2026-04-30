#!/usr/bin/env bash

OWNER=${1?Usage: $0 <owner> <repo> <pr_number>}
REPO=${2?Usage: $0 <owner> <repo> <pr_number>}
PR_NUMBER=${3?Usage: $0 <owner> <repo> <pr_number>}

gh api graphql -F owner="$OWNER" -F repo="$REPO" -F pr="$PR_NUMBER" -f query='
  query($owner: String!, $repo: String!, $pr: Int!) {
    repository(owner: $owner, name: $repo) {
      pullRequest(number: $pr) {
        reviewThreads(first: 100) {
          nodes {
            id
            isResolved
            path
            line
            originalLine
            comments(first: 100) {
              nodes {
                databaseId
                author { login }
                body
              }
            }
          }
        }
      }
    }
  }' --jq '[
    .data.repository.pullRequest.reviewThreads.nodes[]
    | select(.isResolved == false)
    | "ThreadID: \(.id)\nPath: \(.path):\(.line // .originalLine // "unknown")\nComments:\n" + (.comments.nodes | map("  [comment_id=\(.databaseId)] [\(.author.login)]: \(.body | .[0:500])") | join("\n")) + "\n---"
  ] | if length == 0 then "No unresolved reviews" else join("\n") end'
