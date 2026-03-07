#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Usage: $0 <featureName> <templateName>"
    echo "Example: $0 Sample feature"
    exit 1
fi

FEATURE_NAME=$1
TEMPLATE_NAME=$2
BASE_PACKAGE_NAME="com.hand.log"
ROOT_FOLDER_NAME="featureTemplate"

# 프로젝트 루트 디렉터리 찾기
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 경로 설정
TEMPLATE_DIR="$PROJECT_ROOT/$ROOT_FOLDER_NAME/$TEMPLATE_NAME"

# 호환성을 위한 문자열 변환 함수
to_lower() {
    echo "$1" | tr '[:upper:]' '[:lower:]'
}

to_upper_first() {
    echo "$(echo ${1:0:1} | tr '[:lower:]' '[:upper:]')${1:1}"
}

to_lower_first() {
    echo "$(echo ${1:0:1} | tr '[:upper:]' '[:lower:]')${1:1}"
}

LOWER_FEATURE_NAME=$(to_lower "$FEATURE_NAME")
UPPER_CAMEL_CASE_NAME=$(to_upper_first "$FEATURE_NAME")
LOWER_CAMEL_CASE_NAME=$(to_lower_first "$FEATURE_NAME")

# feature 모듈 디렉터리
FEATURE_MODULE_DIR="$PROJECT_ROOT/feature/$LOWER_FEATURE_NAME"

# 템플릿 디렉터리 존재 확인
if [ ! -d "$TEMPLATE_DIR" ]; then
    echo "Error: Template directory '$TEMPLATE_DIR' not found."
    exit 1
fi

# Feature 모듈 디렉터리 생성
mkdir -p "$FEATURE_MODULE_DIR"

# 템플릿 전체 구조를 복사하면서 플레이스홀더 치환
find "$TEMPLATE_DIR" -type f | while read template_file; do
    # 템플릿에서의 상대 경로 계산
    relative_path="${template_file#$TEMPLATE_DIR/}"

    # 파일명에서 플레이스홀더 치환
    target_relative_path=$(echo "$relative_path" | sed -e "s|{{className}}|$UPPER_CAMEL_CASE_NAME|g" \
                                                      -e "s|{{featureName}}|$LOWER_FEATURE_NAME|g" \
                                                      -e "s|{{lowerCamelClassName}}|$LOWER_CAMEL_CASE_NAME|g")

    target_file="$FEATURE_MODULE_DIR/$target_relative_path"

    # 대상 디렉터리 생성
    target_dir="$(dirname "$target_file")"
    mkdir -p "$target_dir"

    # 파일 내용 치환하여 생성
    sed -e "s|{{packageName}}|$BASE_PACKAGE_NAME|g" \
        -e "s|{{featureName}}|$LOWER_FEATURE_NAME|g" \
        -e "s|{{className}}|$UPPER_CAMEL_CASE_NAME|g" \
        -e "s|{{lowerCamelClassName}}|$LOWER_CAMEL_CASE_NAME|g" \
        "$template_file" > "$target_file"

    echo "Generated file: $target_file"
done

# settings.gradle.kts 파일에 include 추가
SETTINGS_GRADLE="$PROJECT_ROOT/settings.gradle.kts"

if [ -f "$SETTINGS_GRADLE" ]; then
    # 이미 해당 모듈이 추가되어 있는지 확인
    if grep -q ":feature:$LOWER_FEATURE_NAME" "$SETTINGS_GRADLE"; then
        echo "Module ':feature:$LOWER_FEATURE_NAME' already exists in settings.gradle.kts"
    else
        # feature 섹션의 마지막 include 뒤에 새 모듈 추가
        sed -i '' '/\/\/ feature/,/)/{
            /)/i\
    ":feature:'"$LOWER_FEATURE_NAME"'",
        }' "$SETTINGS_GRADLE"
        echo "Added ':feature:$LOWER_FEATURE_NAME' to settings.gradle.kts"
    fi
else
    echo "Warning: settings.gradle.kts not found"
fi

echo "Feature '$FEATURE_NAME' generation completed."
echo "Check directory: $FEATURE_MODULE_DIR"
