package hyunsub.board.article.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageLimitCalculator {

    // 현재 페이지 번호, 한 페이지당 게시글 개수, 이동 가능한 페이지 개수 (우리는 10개 기준 이었음.)
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
