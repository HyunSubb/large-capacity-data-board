package hyunsub.board.article.api.data;

import hyunsub.board.article.entity.Article;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuke.board.common.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Spring Boot 애플리케이션 컨텍스트를 로드하여 테스트
@SpringBootTest
public class DataInitializer {
    // JPA의 핵심 객체, 엔티티의 생명주기 관리 (DB 작업)
    @PersistenceContext
    EntityManager entityManager;

    // 트랜잭션을 프로그래밍 방식으로 관리
    /*
        TransactionTemplate: 트랜잭션을 프로그래밍 방식으로 관리하는 Spring의 유틸리티 클래스입니다.
     */
    @Autowired
    TransactionTemplate transactionTemplate;

    // 고유 ID를 생성하는 클래스 (분산 환경에서 사용)
    /*
        Snowflake: snowflake.nextId()를 통해 고유한 ID를 생성하는 클래스입니다.
        분산 환경에서 충돌 없이 고유한 64비트 정수 ID를 생성하는 알고리즘으로,
        주로 대량의 데이터를 처리할 때 사용됩니다.
     */
    Snowflake snowflake = new Snowflake();

    // 멀티 스레드 작업 완료를 기다리는 동기화 도구 (6000번의 작업이 끝날 때까지 대기)
    /*
        CountDownLatch: 다중 스레드 프로그래밍에서 사용되는 동기화 도구입니다.
        latch.await()는 latch.countDown()이 EXECUTE_COUNT(6000번)만큼 호출될 때까지 메인 스레드를 기다리게 합니다.
        즉, 모든 스레드의 작업이 끝날 때까지 테스트가 종료되지 않도록 보장합니다.
     */
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT); // 6000번 실행

    // 한 트랜잭션에 삽입할 데이터의 수
    static final int BULK_INSERT_SIZE = 2000;
    // 전체 트랜잭션 실행 횟수
    static final int EXECUTE_COUNT = 6000;

    /*
    [코드의 동작 흐름]

    initialize() 메서드 실행: for 루프를 통해 EXECUTE_COUNT(6000)번 반복합니다.

    스레드 생성 및 작업 할당: 반복할 때마다 executorService.submit을 통해 스레드 풀에 insert() 메서드를 실행하는 작업을 할당합니다.

    병렬 삽입: 10개의 스레드가 동시에 insert() 메서드를 실행합니다.
    각 스레드는 transactionTemplate을 사용하여 2,000개의 Article 엔티티를 한 트랜잭션 내에서 데이터베이스에 삽입합니다.

    카운트 다운: 하나의 insert() 작업이 완료될 때마다 latch.countDown()이 호출되어 카운트가 1씩 줄어듭니다.

    모든 작업 완료 대기: latch.await()를 통해 모든 6,000번의 insert() 작업이 완료될 때까지 initialize() 메서드의 실행이 멈춥니다.

    스레드 풀 종료: 모든 작업이 완료되면 executorService.shutdown()으로 스레드 풀을 종료합니다.
     */

    @Test
    void initialize() throws InterruptedException {
        // 10개의 스레드를 가진 스레드 풀 생성
        /*
            ExecutorService: 스레드 풀을 관리하는 객체입니다.
            Executors.newFixedThreadPool(10)는 10개의 스레드를 가진 스레드 풀을 생성하여,
            동시에 10개의 스레드가 작업을 처리할 수 있게 합니다.
         */
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // EXECUTE_COUNT(6000)번 반복
        for(int i = 0; i < EXECUTE_COUNT; i++) {
            // 스레드 풀에 insert() 메서드 실행 작업을 할당
            executorService.submit(() -> {
                insert();
                // 작업 완료 후 카운트 다운
                latch.countDown(); // EXECUTE_COUNT (전체 트랜잭션 실행 횟수 1감소)
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        // 모든 스레드 작업이 완료될 때까지 대기
        latch.await();
        // 스레드 풀 종료
        executorService.shutdown();
    }

    // 데이터 삽입 메서드
    // insert() 한 번당 2000건의 데이터 삽입
    void insert() {
        // 하나의 트랜잭션으로 묶어서 실행
        /*
            transactionTemplate.executeWithoutResult는 하나의 트랜잭션으로 묶어 내부 코드를 실행하며,
            모든 작업이 성공적으로 완료되면 커밋하고, 오류 발생 시 롤백합니다.
         */
        transactionTemplate.executeWithoutResult(status -> {
            // BULK_INSERT_SIZE(2000)만큼 반복하며 Article 엔티티 생성
            for(int i = 0; i < BULK_INSERT_SIZE; i++) {
                Article article = Article.create(
                        snowflake.nextId(), // 고유 ID 생성
                        "title" + i,
                        "content" + i,
                        1L,
                        1L
                );
                // 엔티티를 영속성 컨텍스트에 저장 (DB에 저장 준비)
                entityManager.persist(article);
            }
        });
    }
}