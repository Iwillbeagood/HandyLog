## 안드로이드 권장 모듈 템플릿
이 프로젝트는 나중에 다른 프로젝트를 개발할 때, 편리하게 안드로이드 권장 모듈 구조를 구축할 수 있게 미리 정의해논 프로젝트입니다.

프로젝트의 터미널에서 아래의 형식으로 원하는 프로젝트 패키지 명을 입력하면 자동으로 패키지명이 변경됩니다.



⚠️ 경고: build-logic 내의 패키지 명은 변경되지 않으니 이는 직접 수정해야 합니다.

## 명령어
### 패키지명 변경
```bash
/bin/bash scripts/update_package.sh com.hand.log
```

### 새로운 기능 모듈 생성
```bash
/bin/bash scripts/generate-feature.sh Home feature
```

### 모듈 의존성 그래프 생성
아래 명령어로 그래프 이미지를 생성합니다. 결과는 프로젝트 루트에 project.dot.png로 생성됩니다.
```bash
./gradlew projectDependencyGraph
```
