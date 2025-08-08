대용량 데이터 게시판 프로젝트 (Large-Capacity Data Board)
이 프로젝트는 대용량 데이터를 효율적으로 처리하기 위해 마이크로서비스 아키텍처를 기반으로 설계된 게시판 애플리케이션입니다. 여러 서비스가 Gradle 멀티 모듈 구조를 통해 관리됩니다.

주요 기술 스택
언어: Java 21

프레임워크: Spring Boot 3.5.4

빌드 도구: Gradle

ORM: Spring Data JPA, Hibernate

데이터베이스: MySQL

기타: Lombok, Snowflake (고유 ID 생성)

프로젝트 구조
프로젝트는 모듈화된 구조를 가지고 있으며, settings.gradle 파일에 정의된 대로 다음과 같이 구성됩니다:

common: 여러 서비스에서 공통으로 사용되는 모듈을 포함합니다.

common:snowflake: 분산 환경에서 사용할 수 있는 고유 ID를 생성하는 Snowflake 알고리즘 구현체가 포함되어 있습니다.

service: 각 비즈니스 로직을 담당하는 마이크로서비스 모듈을 포함합니다.

service:article: 게시글의 생성, 조회, 수정, 삭제를 담당하는 핵심 서비스. 포트 9000 사용.

service:comment: 댓글 관련 서비스. 포트 9001 사용.

service:like: 좋아요 관련 서비스. 포트 9002 사용.

service:view: 조회수 관련 서비스. 포트 9003 사용.

service:hot-article: 인기 게시글 관련 서비스. 포트 9004 사용.

service:article-read: 게시글 읽기 관련 서비스. 포트 9005 사용.
