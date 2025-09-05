package kuke.board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;

@Getter
public class AssignedShard {
    // 현재 인스턴스(애플리케이션)에 할당된 샤드 목록
    private List<Long> shards;

    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        // List<String> appIds < 현재 실행중인 애플리케이션의 목록들
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    // 실제 샤드 할당 로직
    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        // 현재 인스턴스의 순서를 찾습니다.
        int appIndex = findAppIndex(appId, appIds);
        if (appIndex == -1) {
            return List.of(); // 할당할 샤드가 없으면 빈 목록 반환
        }

        // 전체 샤드 개수를 인스턴스 수로 나누어 각 인스턴스가 담당할 샤드 범위를 계산합니다. -> 모듈러 샤딩 원리를 따르고 있다고 gemini가 그랬음.
        // shardCount / appIds.size(): 이 부분은 **각 인스턴스에 할당될 샤드의 기본 개수(몫)**를 계산합니다. (예: 샤드 64개 / 인스턴스 4대 = 16개)
        // appIndex: 현재 인스턴스의 순서를 나타냅니다. (0번, 1번, 2번...)
        // 이 두 값을 곱하면, 현재 인스턴스보다 앞에 있는 인스턴스들이 몇 개의 샤드를 담당했는지 총합을 구할 수 있다.
        long start = appIndex * shardCount / appIds.size();
        long end = (appIndex + 1) * shardCount / appIds.size() - 1;

        // 계산된 범위에 해당하는 샤드들을 LongStream을 사용해 생성하고 반환합니다.
        return LongStream.rangeClosed(start, end).boxed().toList();
    }

    // Redis에서 가져온 인스턴스 ID 목록에서 현재 인스턴스의 인덱스를 찾습니다.
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i=0; i < appIds.size(); i++) {
            if (appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }
}
