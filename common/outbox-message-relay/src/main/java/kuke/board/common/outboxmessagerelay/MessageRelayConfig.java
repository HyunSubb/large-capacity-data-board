package kuke.board.common.outboxmessagerelay;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
    트랜잭션이 끝나면은 이 카프카에 대한 이벤트 전송을 비동기로 처리한다고 해서 @EnableAsync < 해당 어노테이션을 달아주었다.
 */
@EnableAsync
@Configuration
@ComponentScan("kuke.board.common.outboxmessagerelay")
@EnableScheduling
public class MessageRelayConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /*
        카프카에 대한 설정 해주기.
        이걸로 카프카로 메시지 전송 가능
     */
    @Bean
    public KafkaTemplate<String, String> messageRelayKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    /*
        이 값들은 서버 설정에 따라서 달라질 수도 있다.
        이거는 필요한 서버 스펙에 맞춰서 커스텀할 수 있는 그런 설정이기에 어떤 값인지 보려면 따로 찾아보기 ㄱㄱ

        이건 트랜잭션이 끝날 때마다 이벤트 전송을 비동기로 전송하기 위해서 그걸 처리하는 스레드 풀이다.
     */
    @Bean
    public Executor messageRelayPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mr-pub-event-");
        return executor;
    }

    /*
        이거는 이벤트 전송이 아직 되지 않은 것들 / 10초 이후에 이벤트들은 주기적으로 보내준다고 했었는데 그걸 위한 스레드풀임.
        얘는 싱글 스레드로만 이제 미전송 이벤트들을 전송해주겠다.
     */
    @Bean
    public Executor messageRelayPublishPendingEventExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
