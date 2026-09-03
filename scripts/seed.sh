#!/usr/bin/env bash

set -euo pipefail

usage() {
    cat <<'EOF'
사용법: ./scripts/seed.sh [게시글 수] [게시글당 댓글 수]

환경변수:
  BASE_URL        API 주소 (기본값: http://localhost:8080)
  SEED_PASSWORD   생성 데이터 비밀번호 (기본값: 1234)
  MAX_VIEW_COUNT  게시글별 최대 조회수 (기본값: 100, 최대값: 32767)

예시:
  ./scripts/seed.sh 20 5
  BASE_URL=http://localhost:8081 SEED_PASSWORD=5678 MAX_VIEW_COUNT=500 ./scripts/seed.sh 10 3
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

board_count="${1:-10}"
comments_per_board="${2:-3}"
base_url="${BASE_URL:-http://localhost:8080}"
base_url="${base_url%/}"
password="${SEED_PASSWORD:-1234}"
max_view_count="${MAX_VIEW_COUNT:-100}"

if [[ ! "$board_count" =~ ^[1-9][0-9]*$ ]]; then
    echo "게시글 수는 1 이상의 정수여야 합니다." >&2
    exit 1
fi

if [[ ! "$comments_per_board" =~ ^[0-9]+$ ]]; then
    echo "댓글 수는 0 이상의 정수여야 합니다." >&2
    exit 1
fi

if [[ ! "$max_view_count" =~ ^[0-9]+$ ]] || ((max_view_count > 32767)); then
    echo "MAX_VIEW_COUNT는 0 이상 32767 이하의 정수여야 합니다." >&2
    exit 1
fi

if (( ${#password} < 4 || ${#password} > 16 )); then
    echo "SEED_PASSWORD는 4자 이상 16자 이하여야 합니다." >&2
    exit 1
fi

for command in curl jq; do
    if ! command -v "$command" >/dev/null 2>&1; then
        echo "$command 명령이 필요합니다." >&2
        exit 1
    fi
done

if ! curl -fsS "$base_url/api/boards?page=0&size=1" >/dev/null; then
    echo "API에 연결할 수 없습니다: $base_url" >&2
    exit 1
fi

for ((board_index = 1; board_index <= board_count; board_index++)); do
    board_payload=$(jq -nc \
        --arg title "테스트 게시글 $board_index" \
        --arg content "테스트 게시글 $board_index 본문입니다." \
        --arg writer "테스트 작성자 $board_index" \
        --arg password "$password" \
        '{title: $title, content: $content, writer: $writer, password: $password}')

    board_response=$(curl -fsS -X POST "$base_url/api/boards" \
        -H 'Content-Type: application/json' \
        -d "$board_payload")
    board_id=$(jq -er '.data.id' <<<"$board_response")
    view_count=$((RANDOM % (max_view_count + 1)))

    for ((view_index = 0; view_index < view_count; view_index++)); do
        curl -fsS "$base_url/api/boards/$board_id" >/dev/null
    done

    for ((comment_index = 1; comment_index <= comments_per_board; comment_index++)); do
        comment_payload=$(jq -nc \
            --arg content "게시글 $board_id 테스트 댓글 $comment_index" \
            --arg writer "댓글 작성자 $comment_index" \
            --arg password "$password" \
            '{content: $content, writer: $writer, password: $password}')

        curl -fsS -X POST "$base_url/api/boards/$board_id/comments" \
            -H 'Content-Type: application/json' \
            -d "$comment_payload" >/dev/null
    done

    printf '게시글 %d 생성 완료 (id=%s, 조회수=%d, 댓글 %d개)\n' \
        "$board_index" "$board_id" "$view_count" "$comments_per_board"
done

printf '테스트 데이터 생성 완료: 게시글 %d개, 댓글 %d개\n' \
    "$board_count" "$((board_count * comments_per_board))"
