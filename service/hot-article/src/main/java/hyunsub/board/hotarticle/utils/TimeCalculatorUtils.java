package hyunsub.board.hotarticle.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeCalculatorUtils {
    public static Duration calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);
        // 현재 시간에서 자정까지 몇 시간이 남았는지 구해주는 메서드
        // redis에 넣어줄 ttl 값이 이거다.
        return Duration.between(now, midnight);
    }
}
