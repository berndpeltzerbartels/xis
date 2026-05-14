package one.xis.test.js;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Event {
    @Getter
    private final String eventType;
    @Getter
    private final DataTransfer dataTransfer = new DataTransfer();

    public void preventDefault() {
    }

    public static class DataTransfer {
        private final Map<String, String> data = new HashMap<>();

        public void setData(String type, String value) {
            data.put(type, value);
        }

        public String getData(String type) {
            return data.get(type);
        }
    }
}
