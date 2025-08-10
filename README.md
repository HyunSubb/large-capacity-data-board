# 🚀 대용량 데이터 게시판 (Large-Capacity Data Board)

---

## 📁 프로젝트 구조

```bash
large-capacity-data-board/
├── common/
│   └── snowflake/          # 고유 ID 생성을 위한 공통 모듈
├── service/
│   ├── article/            # 게시글 생성/조회/수정/삭제 서비스 (port: 9000)
│   ├── comment/            # 댓글 서비스 (port: 9001)
│   ├── like/               # 좋아요 서비스 (port: 9002)
│   ├── view/               # 조회수 서비스 (port: 9003)
│   ├── hot-article/        # 인기 게시글 서비스 (port: 9004)
│   └── article-read/       # 게시글 읽기 전용 서비스 (port: 9005)
