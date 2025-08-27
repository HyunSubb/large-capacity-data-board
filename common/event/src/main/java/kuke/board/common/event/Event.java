package kuke.board.common.event;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.Getter;

/*
    <T extends EventPayload>는 제네릭 타입 매개변수로,
    T가 EventPayload 클래스를 상속받는 모든 타입이 될 수 있다는 의미입니다.

    요약하자면, <T extends EventPayload>는 Event 클래스가 다양한 EventPayload 타입의 데이터를 담을 수 있도록
    유연성을 제공하는 동시에, 올바른 타입만 허용하도록 강제하여 코드의 안정성을 높이는 중요한 역할을 합니다.
 */

@Getter
public class Event <T extends EventPayload>{
    // 이벤트 통신에 활용되는 그런 그 통신을 위한 클래스
    private Long eventId;
    private EventType type;
    private T payload;

    public static Event<EventPayload> of (Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    // 이벤트를 카프카로 전달할 때 json으로 변환하고 다시 역직렬화하고 그런식으로 할 거임.

    /*
        이벤트 객체 -> json 로 변환.
    */
    public String toJson() {
        return DataSerializer.serialize(this);
    }

    /*
        json -> 이벤트 객체로 변환.
     */
    public static Event<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }

    /*
        처음에는 카프카에서 가져온 json 데이터를 type(타입) / payload(내용) -> String / Object 로 받아오자.
     */
    @Getter
    private static class EventRaw {
        private Long eventId;
        private String type;
        private Object payload;
    }
}
