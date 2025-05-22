# **Ballog**

# 1. 개요

---

- 스마트워치 기반 풋살 경기 기록 및 분석 서비스
- **Ballog**는 스마트워치만으로 풋살 경기를 기록하고 분석해주는 서비스입니다.
- 고가의 센서 없이, 이미 보급된 스마트워치(Galaxy Watch)를 활용해 경기 데이터를 수집합니다.
- 경기 데이터 분석뿐 아니라, 영상 하이라이트 북마킹, 선수 카드 생성을 지원해 사용자가 손쉽게 퍼포먼스를 확인하고 공유할 수 있습니다.

# 2. 사용 도구

---

- 이슈 관리 : Jira
- 형상 관리 : GitLab
- 커뮤니케이션 : Notion, MatterMost
- 디자인 : Figma
- CI/CD : Gitlab Runner

# 3. 개발 도구

---

- Intellij : 2024.3.1.1
- Android Studio : 2024.3.1

# 4. 개발 환경

---

# 4-1. Android

| 항목 | 버전 |
| --- | --- |
| **Android Studio** | 최신 안정 버전 |
| **Kotlin** | 2.0.21 |
| **Android Gradle Plugin (AGP)** | 8.9.1 |
| **compileSdk** | 34 |
| **minSdk** | 26 |
| **targetSdk** | 34 |

---

## Dependencies

| 라이브러리 | 버전 |
| --- | --- |
| **Jetpack Compose BOM** | 2024.03.00 |
| **Retrofit** | 2.9.0 |
| **OkHttp** | 4.10.0 |
| **Coil (이미지 로딩 라이브러리)** | 2.6.0 |
| **Coroutine** | 1.7.3 |
| **Navigation Compose** | 2.7.7 |
| **Material3** | 1.2.1 |
| **Compose foundation** | 1.4.0 |
| **activity-ktx** | 1.0.0 |
| **aws-android-sdk-s3** | 2.71.0 |
| **datastore-preferences** | 1.0.0 |
| **wearable** | v1800 |
| **media3** | 1.2.1 |

# 4-2. Backend (Spring Boot)

| 항목 | 버전 |
| --- | --- |
| **Java (OpenJDK)** | 17 |
| **Spring Boot** | 3.3.9 |
| **Gradle** | 7.x 이상 |
| **Spring Dependency Management** | 1.1.7 |

---

## Dependencies

| 라이브러리 | 버전 |
| --- | --- |
| **Spring Boot Starter Web** | 최신 안정 버전 |
| **Spring Boot Starter JPA** | 최신 안정 버전 |
| **Spring Boot Starter Security** | 최신 안정 버전 |
| **Spring Boot Starter Webflux** | 최신 안정 버전 |
| **Spring Boot Starter Validation** | 최신 안정 버전 |
| **Spring Boot Starter Mail** | 최신 안정 버전 |
| **Spring Boot Starter Data Redis** | 최신 안정 버전 |
| **QueryDSL** | 5.1.0 (jakarta) |
| **Hibernate Spatial** | 6.4.4.Final |
| **jts-core** | 1.19.0 |
| **Swagger (SpringDoc OpenAPI)** | 2.6.0 |
| **Jsoup** | 1.15.3 |

# 4-3. Wear OS

| 항목 | 버전 |
| --- | --- |
| **Android Studio** | 최신 안정 버전 |
| **Kotlin** | 2.0.21 |
| **Android Gradle Plugin (AGP)** | 8.9.1 |
| **compileSdk** | 35 |
| **minSdk** | 34 |
| **targetSdk** | 35 |

---

## Dependencies

| 라이브러리 | 버전 |
| --- | --- |
| **Compose-Navigation** | 1.2.0 |
| **Play-Services-location** | 21.0.1 |
| **Corutines-play-services** | 1.7.3 |

# 5. 외부 프로그램

---

- **AWS S3**
    - 사진 및 영상 업로드 사용
- **Chat GPT**
    - 플레이스타일 분석 레포트 사용
        - GPT 키 연결하여 사용

# 6. 환경변수 형태

---

- **Backend**
    - **application.yml**
        
        ```jsx
        spring:
          config:
            import: classpath:/env.yml
          application:
            name: ballog
          security:
            filter:
              dispatcher-types: request
          servlet:
            multipart:
              enabled: true
              max-request-size: 300MB
              max-file-size: 300MB
        
          datasource:
              url: ${DB_URL}
              username: ${DB_USERNAME}
              password: ${DB_PASSWORD}
              driver-class-name: org.postgresql.Driver
              hikari:
                max-lifetime: 6000
        
          jpa:
            database: postgresql
            hibernate:
              ddl-auto: ${JPA_DDL_AUTO}
            properties:
              hibernate:
                format_sql: true
                show_sql: true
            open-in-view: false
        
          data:
            redis:
              host: ${REDIS_HOST}
              port: ${REDIS_PORT}
        
          mail:
            host: ${MAIL_HOST}
            port: ${MAIL_PORT}
            username: ${MAIL_USERNAME}
            password: ${MAIL_PASSWORD}
            properties:
              mail:
                debug: true
                smtp:
                  auth: true                  # 인증 필요
                  starttls:
                    enable: true              # TLS 방식 사용
                    required: true
                  connectiontimeout: 10000    # 10초
                  timeout: 10000              # 읽기 타임아웃 10초
                  writetimeout: 10000         # 쓰기 타임아웃 10초
            auth-code-expiration-ms: 300000   # 5분간 유효
        
          cloud:
            aws:
              credentials:
                access-key: ${AWS_ACCESS_KEY}
                secret-key: ${AWS_SECRET_KEY}
              region:
                static: ${AWS_S3_REGION}
              s3:
                bucket: ${AWS_S3_BUCKET}
              stack:
                auto: false
        
        jwt:
          secret: ${JWT_SECRET}
          access-expire-ms: 2592000000      # 15분(개발 단계에서는 1달)
        #  refresh-expire-ms: 2592000000     # refresh 토큰 만료 없음
        
        aws:
          presign:
            expirationMinutes: 60
        
        server:
          servlet:
            context-path: /api
        ```
        

# 7. CI/CD 구축

---

## 1. CI/CD 구성 요약

현재 시스템은 GitLab CI/CD를 활용하여 자동 배포를 구성하고 있으며, Docker Compose로 여러 서비스를 관리합니다.

## 2. 필요한 환경 설정

### 2.1. GitLab 변수 설정

GitLab 프로젝트 설정에서 다음 변수들을 추가해야 합니다:

1. GitLab 프로젝트 > Settings > CI/CD > Variables로 이동
2. 다음 변수들을 추가:

```
SSH_PRIVATE_KEY        # 서버 접속용 SSH 개인키 (전체 내용)
SSH_KNOWN_HOSTS        # 서버 SSH 호스트 정보
SERVER_IP              # 배포 대상 서버 IP 주소

# 데이터베이스 설정
DB_URL                 # 예: jdbc:postgresql://postgresql:5432/ballog
DB_USERNAME            # PostgreSQL 사용자명
DB_PASSWORD            # PostgreSQL 비밀번호

# Redis 설정
REDIS_HOST             # 예: redis
REDIS_PORT             # 예: 6379

# JWT 설정
JWT_SECRET             # JWT 서명 비밀키

# JPA 설정
JPA_DDL_AUTO           # 예: update 또는 validate

# SMTP 메일 설정
MAIL_HOST              # 예: smtp.gmail.com
MAIL_PORT              # 예: 587
MAIL_USERNAME          # 이메일 계정
MAIL_PASSWORD          # 이메일 비밀번호 또는 앱 비밀번호

# AWS S3 설정
AWS_S3_REGION          # 예: ap-northeast-2
AWS_S3_BUCKET          # 버킷 이름
AWS_ACCESS_KEY         # AWS 액세스 키
AWS_SECRET_KEY         # AWS 시크릿 키

# OpenAI 설정
OPENAI_API_KEY         # OpenAI API 키
OPENAI_API_URL         # API URL
OPENAI_MODEL           # 사용할 모델명 (예: gpt-3.5-turbo)

```

### 2.2. 서버 준비 사항

서버에 다음 소프트웨어를 설치해야 합니다:

```bash
# Docker 설치
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL <https://download.docker.com/linux/ubuntu/gpg> | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] <https://download.docker.com/linux/ubuntu> $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install -y docker-ce

# Docker Compose 설치
sudo curl -L "<https://github.com/docker/compose/releases/download/v2.15.1/docker-compose-$>(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Docker 권한 설정
sudo usermod -aG docker ubuntu

```

SSL 인증서 준비:

```bash
# SSL 인증서 디렉토리 생성
sudo mkdir -p /opt/certs
sudo chmod 755 /opt/certs

# 인증서 파일 복사 (예시)
sudo cp fullchain.pem /opt/certs/
sudo cp privkey.pem /opt/certs/
sudo chmod 644 /opt/certs/*.pem

```

프로젝트 클론:

```bash
mkdir -p /home/ubuntu
cd /home/ubuntu
git clone <https://gitlab.com/your-group/S12P31A404.git>
cd S12P31A404

```

## 3. GitLab CI/CD 구성

### 3.1. .gitlab-ci.yml 파일

다음은 GitLab CI/CD 구성 파일입니다:

```yaml
stages:
  - deploy

variables:

deploy:
  stage: deploy
  tags:
    - ballog
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - mkdir -p ~/.ssh
    - echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
    - chmod 600 ~/.ssh/id_rsa
    - echo "$SSH_KNOWN_HOSTS" > ~/.ssh/known_hosts
    - chmod 644 ~/.ssh/known_hosts
  script:
    - |
      ssh -i ~/.ssh/id_rsa ubuntu@$SERVER_IP "
        cd /home/ubuntu/S12P31A404 &&
        git fetch &&
        git checkout $CI_COMMIT_REF_NAME &&
        git pull &&

        # env.yml 파일을 올바른 위치에 생성 (src/main/resources 디렉토리)
        mkdir -p backend/src/main/resources &&
        echo "DB_URL: $DB_URL" > backend/src/main/resources/env.yml &&
        echo "DB_USERNAME: $DB_USERNAME" >> backend/src/main/resources/env.yml &&
        echo "DB_PASSWORD: $DB_PASSWORD" >> backend/src/main/resources/env.yml &&

        # Redis 환경변수 추가
        echo "REDIS_HOST: $REDIS_HOST" >> backend/src/main/resources/env.yml &&
        echo "REDIS_PORT: $REDIS_PORT" >> backend/src/main/resources/env.yml &&

        echo "JWT_SECRET: $JWT_SECRET" >> backend/src/main/resources/env.yml &&
        echo "JPA_DDL_AUTO: $JPA_DDL_AUTO" >> backend/src/main/resources/env.yml &&

        # SMTP 환경변수 추가
        echo "MAIL_HOST: $MAIL_HOST" >> backend/src/main/resources/env.yml &&
        echo "MAIL_PORT: $MAIL_PORT" >> backend/src/main/resources/env.yml &&
        echo "MAIL_USERNAME: $MAIL_USERNAME" >> backend/src/main/resources/env.yml &&
        echo "MAIL_PASSWORD: $MAIL_PASSWORD" >> backend/src/main/resources/env.yml &&

        echo "AWS_S3_REGION: $AWS_S3_REGION" >> backend/src/main/resources/env.yml &&
        echo "AWS_S3_BUCKET: $AWS_S3_BUCKET" >> backend/src/main/resources/env.yml &&
        echo "AWS_ACCESS_KEY: $AWS_ACCESS_KEY" >> backend/src/main/resources/env.yml &&
        echo "AWS_SECRET_KEY: $AWS_SECRET_KEY" >> backend/src/main/resources/env.yml &&

        echo "OPENAI_API_KEY: $OPENAI_API_KEY" >> backend/src/main/resources/env.yml &&
        echo "OPENAI_API_URL: $OPENAI_API_URL" >> backend/src/main/resources/env.yml &&
        echo "OPENAI_MODEL: $OPENAI_MODEL" >> backend/src/main/resources/env.yml &&

        # 환경 변수 파일 생성 (.env 파일은 docker-compose에서 사용)
        echo "DB_URL=$DB_URL" > .env &&
        echo "DB_USERNAME=$DB_USERNAME" >> .env &&
        echo "DB_PASSWORD=$DB_PASSWORD" >> .env &&

        # 백엔드 서비스 재빌드 및 배포
        echo "$CI_COMMIT_REF_NAME 브랜치: 백엔드 서비스 빌드 및 배포 중..." &&
        docker-compose stop backend nginx &&
        docker-compose rm -f backend nginx &&
        docker-compose build backend nginx &&
        docker-compose up -d backend nginx

        # 사용하지 않는 이미지 정리
        echo "사용하지 않는 Docker 이미지 정리 중..." &&
        docker image prune -af
      "

  only:
    - dev/be

```

### 3.2. GitLab CI/CD 실행 원리

1. `dev/be` 브랜치에 변경 사항이 푸시되면 파이프라인 실행
2. Alpine 이미지 기반 컨테이너에서 SSH 클라이언트 설치 및 환경 구성
3. 서버에 SSH로 접속하여 스크립트 실행
4. 환경 변수 파일 생성 및 Docker Compose를 통해 서비스 재배포

## 4. Docker Compose 구성

### 4.1. docker-compose.yml 파일

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgresql
      - redis

  nginx:
    image: nginx:1.25.3-alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
      - /opt/certs:/certs:ro
    depends_on:
      - backend
    restart: unless-stopped

  postgresql:
    image: postgis/postgis:15-3.4
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - POSTGRES_DB=ballog
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7.0
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

  portainer:
    image: portainer/portainer-ce:latest
    ports:
      - "9000:9000"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - portainer_data:/data
    restart: always

volumes:
  postgres_data:
  redis_data:
  portainer_data:

```

### 4.2. Nginx 설정 파일 (예시)

`nginx/default.conf` 파일을 다음과 같이 작성해야 합니다:

```
server {
    listen 80;
    server_name _;

    # HTTP를 HTTPS로 리다이렉트
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name _;

    # SSL 인증서 설정
    ssl_certificate /certs/fullchain.pem;
    ssl_certificate_key /certs/privkey.pem;

    # SSL 설정
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    # 백엔드 프록시 설정
    location /api {
        proxy_pass <http://backend:8080>;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 정적 파일 제공 (프론트엔드가 있다면)
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
}

```

### 4.3. 백엔드 Dockerfile (예시)

`backend/Dockerfile`을 다음과 같이 작성해야 합니다:

```
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

```

## 5. 수동 배포 방법

GitLab CI/CD 없이 수동으로 배포하려면:

```bash
# 프로젝트 클론
git clone <https://gitlab.com/your-group/S12P31A404.git>
cd S12P31A404

# 환경 설정 파일 생성
mkdir -p backend/src/main/resources
touch backend/src/main/resources/env.yml

# env.yml 파일 작성 (예시)
cat > backend/src/main/resources/env.yml << EOF
DB_URL: jdbc:postgresql://postgresql:5432/ballog
DB_USERNAME: postgres
DB_PASSWORD: your_password
REDIS_HOST: redis
REDIS_PORT: 6379
JWT_SECRET: your_jwt_secret
JPA_DDL_AUTO: update
MAIL_HOST: smtp.gmail.com
MAIL_PORT: 587
MAIL_USERNAME: your_email@gmail.com
MAIL_PASSWORD: your_app_password
AWS_S3_REGION: ap-northeast-2
AWS_S3_BUCKET: your_bucket_name
AWS_ACCESS_KEY: your_access_key
AWS_SECRET_KEY: your_secret_key
OPENAI_API_KEY: your_openai_key
OPENAI_API_URL: <https://api.openai.com>
OPENAI_MODEL: gpt-3.5-turbo
EOF

# .env 파일 생성
cat > .env << EOF
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_URL=jdbc:postgresql://postgresql:5432/ballog
EOF

# Docker Compose 실행
docker-compose up -d

```

## 6. 서비스 관리 명령어

```bash
# 모든 서비스 시작
docker-compose up -d

# 특정 서비스만 시작
docker-compose up -d backend nginx

# 로그 확인
docker-compose logs -f backend

# 서비스 재시작
docker-compose restart backend

# 서비스 중지
docker-compose stop

# 서비스 중지 및 컨테이너 삭제
docker-compose down

# 볼륨을 포함하여 모든 것 삭제 (데이터 손실 주의!)
docker-compose down -v

```

## 7. Portainer 접속 방법

Portainer는 Docker 컨테이너를 GUI로 관리할 수 있는 도구입니다:

1. 브라우저에서 `http://[서버IP]:9000` 접속
2. 초기 설정 시 관리자 계정 생성
3. 대시보드에서 컨테이너, 이미지, 볼륨 등을 관리

## 8. 문제 해결

### 8.1. CI/CD 파이프라인 실패

GitLab CI/CD 파이프라인이 실패할 경우:

1. GitLab 프로젝트 > CI/CD > Pipelines에서 로그 확인
2. SSH 키 설정 문제 확인
3. 환경 변수가 올바르게 설정되었는지 확인

### 8.2. Docker 컨테이너 문제

```bash
# 컨테이너 상태 확인
docker-compose ps

# 컨테이너 로그 확인
docker-compose logs -f backend

# 컨테이너 재시작
docker-compose restart backend

```

### 8.3. SSL 인증서 문제

```bash
# 인증서 파일 확인
ls -la /opt/certs/

# 권한 설정
sudo chmod 644 /opt/certs/*.pem
sudo chmod 755 /opt/certs/

```

### 8.4. 데이터베이스 연결 문제

```bash
# PostgreSQL 컨테이너 접속
docker-compose exec postgresql psql -U postgres

# 데이터베이스 존재 확인
\\l

# 데이터베이스 선택
\\c ballog

# 테이블 목록 확인
\\dt

```