package hyunsub.board.view.repository;

import hyunsub.board.view.entity.ArticleViewCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArticleViewCountBackUpRepositoryTest {
    @Autowired
    ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
    @PersistenceContext
    EntityManager entityManager;

    @Test
    @Transactional
    void updateViewCountTest() {
        articleViewCountBackUpRepository.save(
                ArticleViewCount.init(1L, 1L)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        // 왜 이런 조건이 필요하냐면 이게 동시에 요청이 여러 번 들어오면은 조회수 집계가 엄청 빠르게 올라갈 수 있음.
        // 그래서 이거 백업하는 시점에 이 백업 코드가 호출될 때 100이 호출된 다음 300이 호출되고,
        // 그 다음에 200에 대해서 카운트가 업데이트 하려고 시도를 할 수 있다. 그런거에 대해서 방어 코드라고 보면 됨.
        int result1 = articleViewCountBackUpRepository.updateViewCount(1L, 100L);
        int result2 = articleViewCountBackUpRepository.updateViewCount(1L, 300L);
        int result3 = articleViewCountBackUpRepository.updateViewCount(1L, 200L);

        // then
        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(0);

        ArticleViewCount articleViewCount = articleViewCountBackUpRepository.findById(1L).get();
        assertThat(articleViewCount.getViewCount()).isEqualTo(300L);
    }
}