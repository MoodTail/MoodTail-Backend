# 월간 리포트 공유 이미지 수명 주기

월간 리포트 공유 이미지는 기존 `public/reports/monthly/` 경로에 저장되며 30일 동안 보관한다.
`docs/s3-monthly-report-lifecycle.json`은 이 경로의 현재 객체와, 버전 관리가 켜진 버킷의 이전 객체 버전을 각각 30일 경과 후 S3 Lifecycle 처리 시점에 삭제하는 규칙이다.

애플리케이션은 이미지 업로드 시 공유 토큰과 `/share/reports/monthly/{shareToken}` 형식의 URL을 반환한다. 공개 응답과 OG 메타데이터에는 S3 원본 URL 대신 `/api/v1/reports/monthly/shares/{shareToken}/image` 형식의 토큰 검증 이미지 URL을 사용한다. 공유 토큰 조회는 생성 후 30일까지만 허용하며, 만료된 공유 이미지의 실제 S3 객체 삭제는 아래 Lifecycle 규칙의 실행 시점에 처리된다.

회원 탈퇴 또는 업로드 이후 DB 저장 실패 시에는 애플리케이션이 관련 S3 객체 삭제를 즉시 시도한다. 공개 API 응답과 OG 페이지에서는 S3 원본 URL을 반환하지 않고 토큰 검증 이미지 엔드포인트를 사용한다.

이 정책은 애플리케이션의 업로드 로직이 아니라 실제 S3 버킷에 설정해야 한다. 운영 버킷의 **Management > Lifecycle rules**에서 다음 값으로 규칙을 추가한다.

- Rule ID: `expire-monthly-report-share-images-after-30-days`
- Scope: Prefix `public/reports/monthly/`
- Expire current versions: 30 days after object creation
- Permanently delete noncurrent versions: 30 days after becoming noncurrent

버킷에 다른 Lifecycle 규칙이 없다면 AWS CLI로도 적용할 수 있다.

```shell
aws s3api put-bucket-lifecycle-configuration \
  --bucket "$AWS_S3_BUCKET" \
  --lifecycle-configuration file://docs/s3-monthly-report-lifecycle.json
```

`put-bucket-lifecycle-configuration`은 기존 Lifecycle 설정 전체를 교체한다. 기존 규칙이 있는 버킷에서는 위 명령을 그대로 실행하지 말고, AWS Console에서 규칙을 추가하거나 기존 설정의 `Rules` 배열에 이 규칙을 병합한 뒤 적용해야 한다.

적용 결과는 다음 명령으로 확인한다.

```shell
aws s3api get-bucket-lifecycle-configuration --bucket "$AWS_S3_BUCKET"
```
