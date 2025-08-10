package hyunsub.board.article.repository;

import hyunsub.board.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {


    // 쿼리는 navtive query를 사용할 거다.
    // Pageable을 사용하면 우리가 학습했던 그 최적화된 쿼리가 만들어지지 않는다.
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from (" +
                        "   select article_id from article " +
                        "   where board_id = :boardId " +
                        "   order by article_id desc " +
                        "   limit :limit offset :offset " +
                    ") t left join article on t.article_id = article.article_id ",
            nativeQuery = true
    )
    List<Article> findAll(
            @Param("boardId") Long boardId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    @Query(
            value = "select count(*) from (" +
                    "   select article_id from article where board_id = :boardId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(@Param("boardId") Long boardId, @Param("limit") Long limit);



}
