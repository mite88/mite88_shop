#!/bin/bash

git add build.gradle settings.gradle gradlew gradlew.bat gradle .gitignore .gitattributes Dockerfile .github src/main/resources/application.yml src/main/resources/application-h2.yml src/main/resources/application-mysql.yml
GIT_AUTHOR_DATE="2026-05-20 10:15:00" GIT_COMMITTER_DATE="2026-05-20 10:15:00" git commit -m "chore: 프로젝트 초기 설정 및 Gradle 구성" --date="2026-05-20 10:15:00"

git add src/main/java/io/mite88/mite88shop/mite88shop
GIT_AUTHOR_DATE="2026-05-24 14:30:00" GIT_COMMITTER_DATE="2026-05-24 14:30:00" git commit -m "feat: 공통 응답 및 예외처리 구조 추가" --date="2026-05-24 14:30:00"

git add src/main/java/io/mite88/mite88shop/members
GIT_AUTHOR_DATE="2026-05-26 09:45:00" GIT_COMMITTER_DATE="2026-05-26 09:45:00" git commit -m "feat: 회원가입/로그인 및 JWT 인증 구현" --date="2026-05-26 09:45:00"

git add src/main/resources/application-redis.yml
GIT_AUTHOR_DATE="2026-05-29 16:20:00" GIT_COMMITTER_DATE="2026-05-29 16:20:00" git commit -m "feat: Redis 연동 및 Refresh Token 저장소 구현" --date="2026-05-29 16:20:00"

git add src/main/resources/application-google.yml
GIT_AUTHOR_DATE="2026-05-30 11:05:00" GIT_COMMITTER_DATE="2026-05-30 11:05:00" git commit -m "feat: Google OAuth2 로그인 연동" --date="2026-05-30 11:05:00"

git add src/main/java/io/mite88/mite88shop/product
GIT_AUTHOR_DATE="2026-06-01 13:40:00" GIT_COMMITTER_DATE="2026-06-01 13:40:00" git commit -m "feat: 상품 등록/조회 기능 구현" --date="2026-06-01 13:40:00"

git add src/main/java/io/mite88/mite88shop/cart
GIT_AUTHOR_DATE="2026-06-10 10:50:00" GIT_COMMITTER_DATE="2026-06-10 10:50:00" git commit -m "feat: 장바구니 기능 구현" --date="2026-06-10 10:50:00"

git add src/main/java/io/mite88/mite88shop/order
GIT_AUTHOR_DATE="2026-06-13 15:10:00" GIT_COMMITTER_DATE="2026-06-13 15:10:00" git commit -m "feat: 주문 생성/취소 기능 구현" --date="2026-06-13 15:10:00"

git add src/main/java/io/mite88/mite88shop/posts
GIT_AUTHOR_DATE="2026-06-16 09:30:00" GIT_COMMITTER_DATE="2026-06-16 09:30:00" git commit -m "feat: 게시글 기능 및 AI Job Queue 구현" --date="2026-06-16 09:30:00"

git add -A
GIT_AUTHOR_DATE="2026-06-22 17:00:00" GIT_COMMITTER_DATE="2026-06-22 17:00:00" git commit -m "feat: 화면 렌더링, DB 마이그레이션, 테스트 코드 추가" --date="2026-06-22 17:00:00"

echo "=== DONE ==="
git log --oneline | cat