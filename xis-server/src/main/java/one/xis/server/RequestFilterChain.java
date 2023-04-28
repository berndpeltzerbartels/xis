package one.xis.server;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestFilterChain {
    private boolean interrupt;
    private int httpStatus = 200;
    private Map<String, Object> data = new HashMap<>();

    public void addData(String name, Object value) {
        data.put(name, value);
    }

    public void interrupt(int httpStatus) {
        this.interrupt = true;
        this.httpStatus = httpStatus;
    }

}
