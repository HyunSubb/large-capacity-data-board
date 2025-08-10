package hyunsub.board.article.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PageLimitCalculatorTest {

    @Test
    void calculatePageLimitTest() {
        // 1번 페이지에 있고 페이지당 30개의 호출을 조회하고 이동 가능한 페이지가 10개라면 301개가 조회가 되어야 한다.
        // 301개가 있다면 다음 버튼 활성화
        calculatePageLimitTest(1L, 30L, 10L, 301L);
        calculatePageLimitTest(7L, 30L, 10L, 301L);
        calculatePageLimitTest(10L, 30L, 10L, 301L);
        calculatePageLimitTest(11L, 30L, 10L, 601L);
        calculatePageLimitTest(12L, 30L, 10L, 601L);
    }

    void calculatePageLimitTest(Long page, Long pageSize, Long movablePageCount, Long expected) {
        Long result = PageLimitCalculator.calculatePageLimit(page, pageSize, movablePageCount);
        assertThat(result).isEqualTo(expected);
    }


}