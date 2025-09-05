package hyunsub.board.hyunsub.outboxmessagerelay;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageRelayConstants {
    /*
        샤딩을 직접 구현하고 있지는 않지만, 샤딩이 되어 있는 상황으로 가정하고 만듦.
        
        샤드 개수를 임의로 4개로 가정
     */
    public static final int SHARD_COUNT = 4; // 임의의 값
}
