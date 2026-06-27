# MoodTail Backend
UMC 10th 프로젝트 무드테일 - Backend
개발 기간 2026.06.26~

---

## 기술 스택
- Java / Spring Boot
- MySQL
- Spring Security

---

## Git Convention

이 문서는 Moodtail 프로젝트의 Git 브랜치 전략, 커밋 메시지 규칙, Pull Request 규칙, 병합 및 배포 흐름을 정리한다.

Moodtail은 `develop` 브랜치를 기준으로 기능을 통합하고, PR 리뷰 후 `develop`에 병합한다. 최종 검토가 끝난 안정 버전만 `main`에 병합한다.

### 1. Branch Strategy

```text
main        운영 배포 브랜치
develop     개발 통합 브랜치
feat/*      기능 개발 브랜치
fix/*       버그 수정 브랜치
docs/*      문서 추가 또는 수정 브랜치
style/*     코드 포맷팅, import 정리 등 로직 변경 없는 수정 브랜치
refactor/*  기능 변경 없는 코드 구조 개선 브랜치
test/*      테스트 코드 추가 또는 수정 브랜치
chore/*     설정, 패키지, 빌드, CI/CD 등 기타 작업 브랜치
hotfix/*    운영 긴급 수정 브랜치
```

브랜치 prefix, 커밋 type, PR type은 같은 목록을 사용한다.

```text
feat
fix
docs
style
refactor
test
chore
hotfix
```

### 2. Branch Naming

브랜치 이름은 작업 목적이 드러나도록 짧고 명확하게 작성한다.

```text
{type}/{work-summary}
```

브랜치명은 kebab-case를 사용한다.

### 3. Development Flow

기능 개발, 버그 수정, 문서 작업, 테스트 작업, 기타 작업은 `develop` 브랜치에서 분기해서 진행한다.

```bash
git checkout develop
git pull origin develop

git checkout -b feat/mood-test
```

작업이 완료되면 원격 브랜치에 push하고, GitHub에서 `develop` 브랜치로 Pull Request를 생성한다.

```bash
git push origin feat/mood-test
```

PR은 리뷰를 받은 뒤 `develop` 브랜치에 병합한다.

```text
feature branch -> Pull Request -> review -> develop
```

`develop`에 병합된 기능들이 최종 검토를 통과하면 `main` 브랜치로 병합한다.

```text
develop -> final review -> main
```

`main` 브랜치는 운영 배포 브랜치이므로 직접 push하지 않는다.

### 4. Protected Branch Rule

`main` 브랜치에는 직접 push하지 않는다.

```text
Do not push directly to main.
```

`develop` 브랜치에도 직접 작업 커밋을 push하지 않는다. 모든 작업은 작업 브랜치에서 진행하고 PR 리뷰 후 `develop`에 병합한다.

권장 보호 규칙은 다음과 같다.

```text
main
- direct push 금지
- PR merge만 허용
- 최종 검토 후 develop에서 main으로 병합
- CI 성공 후 병합

develop
- 직접 작업 커밋 push 금지
- PR merge만 허용
- 리뷰 승인 후 병합
- CI 성공 후 병합
```

### 5. Commit Message Convention

커밋 메시지는 아래 형식을 사용한다.

```text
type: subject
```

`subject`는 변경 내용을 명령형 또는 명사형으로 짧게 작성한다.

```text
feat: 소셜 로그인 API 추가
feat: 감정 테스트 제출 기능 추가
feat: 칵테일 추천 알고리즘 구현
fix: 추천 칵테일 정렬 오류 수정
docs: Swagger 컨벤션 문서 추가
style: import 순서 정리
refactor: 추천 서비스 책임 분리
test: 맛 지표 계산 테스트 추가
chore: Spring Security 설정 추가
hotfix: 운영 OAuth 토큰 오류 긴급 수정
```

### 6. Commit Types

```text
feat      새로운 기능 추가
fix       버그 수정
docs      문서 추가 또는 수정
style     코드 포맷팅, import 정리 등 로직 변경 없는 수정
refactor  기능 변경 없는 코드 구조 개선
test      테스트 코드 추가 또는 수정
chore     설정, 패키지, 빌드, CI/CD 등 기타 작업
hotfix    운영 긴급 수정
```

### 7. Issue Template

이슈는 아래 양식을 사용한다.

```md
---
name: Backend Task
about: Moodtail 백엔드 작업 또는 이슈를 등록합니다.
title: "[FEAT] "
---

### 작업 내용
- 

### 작업 범위
- 도메인:
- API:
- 화면/기능 ID:

### 참고 사항
- 
```

이슈 제목의 type은 PR type과 동일하게 작성한다.

```text
[FEAT] 감정 테스트 제출 API 추가
[FIX] 히스토리 미래 날짜 저장 오류 수정
[DOCS] 추천 API Swagger 문서 수정
[TEST] 추천 서비스 테스트 추가
```

### 8. Pull Request Template

PR 제목은 아래 형식을 사용한다.

```text
[TYPE] subject
```

예시는 다음과 같다.

```text
[FEAT] 카카오 소셜 로그인 API 추가
[FEAT] 감정 테스트 결과 저장 API 추가
[FEAT] 유클리드 거리 기반 추천 로직 추가
[FIX] 칵테일 일치율 계산 오류 수정
[DOCS] Swagger 추천 API 문서 수정
[STYLE] 코드 포맷팅 정리
[REFACTOR] 추천 서비스 구조 개선
[TEST] 칵테일 추천 단위 테스트 추가
[CHORE] CI 설정 추가
[HOTFIX] 운영 배포 오류 긴급 수정
```


### 참고 사항
- 
```

## 9. Code Review Rule

PR은 코드리뷰를 거친 뒤 병합한다.

기본 규칙은 다음과 같다.

```text
- PR은 최소 1명 이상의 approve를 받은 뒤 merge한다.
- PR 작성자는 본인 PR을 리뷰 없이 직접 merge하지 않는다.
- 리뷰어가 남긴 코멘트를 반영한 뒤 해당 conversation을 resolve한다.
- 기능 변경이 있으면 테스트 결과와 Swagger 반영 여부를 함께 확인한다.
- API 변경이 있으면 API 명세서와 Swagger 문서를 함께 수정한다.
- DB 구조 변경이 있으면 ERD 명세서와 migration 계획을 함께 확인한다.
- 긴급 hotfix는 예외적으로 빠르게 merge할 수 있으나, 사후 리뷰를 반드시 진행한다.
```

리뷰어는 다음 기준으로 확인한다.

```text
- 요구사항과 구현이 일치하는가
- 공통 응답과 에러 처리 규칙을 따르는가
- Controller, Service, Repository 책임이 분리되어 있는가
- DTO validation과 비즈니스 validation이 적절히 나뉘어 있는가
- 테스트가 필요한 로직에 테스트가 추가되어 있는가
- Swagger 문서가 실제 API와 일치하는가
- 민감 정보가 커밋되지 않았는가
```

### 11. Merge Rule

```text
feat/*      -> develop
fix/*       -> develop
docs/*      -> develop
style/*     -> develop
refactor/*  -> develop
test/*      -> develop
chore/*     -> develop
hotfix/*    -> main, develop
develop     -> main
```

`main`에는 직접 작업하지 않는다.

일반 작업은 다음 흐름을 따른다.

```text
develop -> work branch -> PR review -> develop -> final review -> main
```

긴급 수정이 필요한 경우 `hotfix/*` 브랜치에서 작업하고, 수정 사항을 `main`과 `develop` 양쪽에 반영한다.

```text
main -> hotfix/* -> PR review -> main
main -> hotfix/* -> develop
```

### 12. Deploy Rule

배포 대상은 `main` 브랜치다.

```text
main merge -> CI/CD 실행 -> 운영 배포
```

배포 전 최종 검토 항목은 다음과 같다.

```text
- develop 브랜치 기능 검증 완료
- PR 리뷰 반영 완료
- CI 통과
- 주요 API Swagger 확인
- 환경 변수 및 민감 정보 커밋 여부 확인
- main 병합 전 최종 동작 확인
```

배포 실패가 발생하면 `main` 브랜치 기준으로 원인을 파악하고 수정한다.


#### 문서
- [Swagger Convention](docs/swagger-convention.md)
- [Backend Coding Convention](docs/backend-coding-convention.md)