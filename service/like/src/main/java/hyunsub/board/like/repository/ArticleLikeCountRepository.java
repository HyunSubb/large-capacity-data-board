package hyunsub.board.like.repository;

import hyunsub.board.like.entity.ArticleLikeCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeCountRepository extends JpaRepository<ArticleLikeCount, Long> {
// select ... for update
// 비관적 락 방법 2에서 위에 있는 for update를 사용한다고 했었는데 @Lock 이라는 어노케이션을 달면 자동으로 처리할 수 있다.
// 그래서 이렇게 lock 모드 타입을 pessimistic lock 이거 해주면 이 query 뒤에 자동으로 for update가 붙어서 Lock이 잡히게 됨.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleLikeCount> findLockedByArticleId(Long articleId);

    @Query(
            value = "update article_like_count set like_count = like_count + 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("articleId") Long articleId);

    @Query(
            value = "update article_like_count set like_count = like_count - 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("articleId") Long articleId);
}
