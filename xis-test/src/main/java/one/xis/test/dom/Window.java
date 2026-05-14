package one.xis.test.dom;

import lombok.Getter;
import one.xis.test.js.Console;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Window extends GraalVMProxy {

    @Getter
    public final Location location;

    @Getter
    public final Document document;

    @Getter
    public final LocalStorage localStorage;

    @Getter
    public final SessionStorage sessionStorage;

    @Getter
    public final Console console;

    @Getter
    public History history = new History();
    private int scrollX = 0;
    private int scrollY = 0;
    private int scrollToCallCount = 0;
    private final Map<String, Object> dynamicMembers = new HashMap<>();

    public Window(Location location) {
        this(location, null, null, null, null);
    }

    public Window(Location location, Document document, LocalStorage localStorage, SessionStorage sessionStorage, Console console) {
        this.location = location;
        this.document = document;
        this.localStorage = localStorage;
        this.sessionStorage = sessionStorage;
        this.console = console;
    }

    public void reset() {
        location.reset();
        history.reset();
        scrollX = 0;
        scrollY = 0;
        scrollToCallCount = 0;
    }

    public void addEventListener(String type, Consumer<?> listener) {
        // No-op
    }

    public void scrollTo(int x, int y) {
        this.scrollX = x;
        this.scrollY = y;
        this.scrollToCallCount++;
    }

    public int getScrollX() {
        return scrollX;
    }

    public int getScrollY() {
        return scrollY;
    }

    public int getScrollToCallCount() {
        return scrollToCallCount;
    }

    @Override
    public Object getMember(String key) {
        if (dynamicMembers.containsKey(key)) {
            return dynamicMembers.get(key);
        }
        return super.getMember(key);
    }

    @Override
    public boolean hasMember(String key) {
        return dynamicMembers.containsKey(key) || super.hasMember(key);
    }

    @Override
    public Object getMemberKeys() {
        var keys = (String[]) super.getMemberKeys();
        var result = new String[keys.length + dynamicMembers.size()];
        System.arraycopy(keys, 0, result, 0, keys.length);
        var index = keys.length;
        for (String key : dynamicMembers.keySet()) {
            result[index++] = key;
        }
        return result;
    }

    @Override
    public void putMember(String key, Value value) {
        if (super.hasMember(key)) {
            super.putMember(key, value);
            return;
        }
        dynamicMembers.put(key, value);
    }
}
