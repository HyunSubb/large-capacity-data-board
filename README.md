# 대용량 데이터 게시판 프로젝트 (Large-Capacity Data Board)

📖 프로젝트 소개
이 프로젝트는 대량의 데이터를 효율적으로 처리할 수 있도록 마이크로서비스 아키텍처와 데이터 샤딩 개념을 적용하여 설계된 게시판 애플리케이션입니다. Gradle 멀티 모듈 기능을 활용하여 각 서비스를 독립적으로 관리하고 확장할 수 있습니다.

📁 프로젝트 구조
프로젝트는 Gradle 멀티 모듈로 구성되어 있으며, 각 모듈의 역할은 다음과 같습니다.

|------|------|
|common:| 여러 서비스에서 공통으로 사용되는 모듈|

common:snowflake: 분산 환경에서 고유 ID를 생성하는 Snowflake 클래스.

service: 각 비즈니스 로직을 담당하는 마이크로서비스 모듈

service:article: 게시글의 생성, 조회, 수정, 삭제를 담당하는 핵심 서비스. (server.port: 9000)

service:comment: 댓글 관련 서비스. (server.port: 9001)

service:like: 좋아요 관련 서비스. (server.port: 9002)

service:view: 조회수 관련 서비스. (server.port: 9003)

service:hot-article: 인기 게시글 관련 서비스. (server.port: 9004)

service:article-read: 게시글 읽기 관련 서비스. (server.port: 9005)
