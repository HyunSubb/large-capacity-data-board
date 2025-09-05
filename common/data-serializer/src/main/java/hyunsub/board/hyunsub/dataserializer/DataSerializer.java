package hyunsub.board.hyunsub.dataserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class DataSerializer {
    private static final ObjectMapper objectMapper = initialize();

    private static ObjectMapper initialize() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // 역직렬화 할 때 없는 필드가 있으면은 에러가 날 수 있다.
        // 그래서 false 로 설정해서 꺼두면 에러가 안남.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    /*
        역직렬화 : 직렬화의 반대 과정. 데이터를 원래의 객체나 데이터 구조로 복구하는 과정을 의미.
        String or 객체를 받아서 특정 클래스 타입으로 역직렬화 해줄 거임.

        역직렬화 : String -> Object / JSON을 읽은 다음 객체로 변환하는 것이니까 READ
     */
    public static <T> T deserialize(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.deserialize] data={}, clazz={}", data, clazz, e);
            return null;
        }
    }

    public static <T> T deserialize(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    /*
        직렬화 : 데이터 구조나 객체 상태를 전송할 수 있는 포맷(ex : JSON)으로 변환.

        직렬화 Object -> String / 객체를 JSON으로 적는 것이니까 WRITE
     */
    public static String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.serialize] object={}", object, e);
            return null;
        }
    }
}
