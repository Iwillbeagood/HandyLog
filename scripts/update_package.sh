#!/bin/bash

export LC_ALL=C

OLD_PACKAGE="com.hand.log"

PROJECT_DIR="/Users/baejunhyeong/AndroidStudioProjects/pre"

if [ -n "$1" ]; then
  NEW_PACKAGE="$1"
else
  read -p "새 패키지명 입력 (예: com.example.app): " NEW_PACKAGE
fi

if [ -z "$NEW_PACKAGE" ]; then
  echo "새 패키지명이 입력되지 않았습니다. 종료합니다."
  exit 1
fi

OLD_PACKAGE_PATH="${OLD_PACKAGE//./\/}"
NEW_PACKAGE_PATH="${NEW_PACKAGE//./\/}"

find "$PROJECT_DIR" -type d -path "*/src/main/java/$OLD_PACKAGE_PATH*" | \
  sort -r | while read -r OLD_PATH; do
    NEW_PATH="${OLD_PATH//$OLD_PACKAGE_PATH/$NEW_PACKAGE_PATH}"
    mkdir -p "$(dirname "$NEW_PATH")"   # 상위 폴더 생성
    mv "$OLD_PATH" "$NEW_PATH"
done

grep -rl "$OLD_PACKAGE" "$PROJECT_DIR" | xargs sed -i "" "s|$OLD_PACKAGE|$NEW_PACKAGE|g"

find "$PROJECT_DIR" -type d -path "*/src/main/java/$OLD_PACKAGE_PATH" | while read -r OLD_DIR; do
  rm -rf "$OLD_DIR"
done

echo "패키지 이름 및 폴더 구조 변경 완료!"
