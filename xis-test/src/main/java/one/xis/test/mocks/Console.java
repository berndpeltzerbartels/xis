package one.xis.test.mocks;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Console {

    @Getter
    private List<Object> infoLog = new ArrayList<>();

    public void log(Object o) {
        infoLog.add(o);
    }
}
