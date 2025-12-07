package one.xis.test.dom;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class History {

    @Getter
    public static class Entry {
        Map<String, Object> state;
        String title;
        String id;

        Entry(Map<String, Object> state, String title, String id) {
            this.state = state;
            this.title = title;
            this.id = id;
        }
    }

    @Getter
    private final List<Entry> entries = new ArrayList<>();
    private int currentIndex = -1;

    public void pushState(Map<String, Object> state, String title, String id) {
        // Entferne alle „zukünftigen“ Einträge
        while (entries.size() > currentIndex + 1) {
            entries.remove(entries.size() - 1);
        }
        entries.add(new Entry(state, title, id));
        currentIndex++;
    }

    public void pushState(Map<String, Object> state, String title) {
        pushState(state, title, null); // ID optional
    }

    public void replaceState(Map<String, Object> state, String title, String id) {
        if (currentIndex >= 0) {
            entries.set(currentIndex, new Entry(state, title, id));
        } else {
            pushState(state, title, id); // keine aktuelle → als push behandeln
        }
    }

    public void reset() {
        entries.clear();
        currentIndex = -1;
    }

    public Map<String, Object> getCurrentState() {
        if (currentIndex >= 0 && currentIndex < entries.size()) {
            return entries.get(currentIndex).state;
        }
        return null;
    }

    public String getCurrentId() {
        if (currentIndex >= 0 && currentIndex < entries.size()) {
            return entries.get(currentIndex).id;
        }
        return null;
    }

    public boolean canGoBack() {
        return currentIndex > 0;
    }

    public void back() {
        if (canGoBack()) {
            currentIndex--;
        }
    }

    public boolean canGoForward() {
        return currentIndex < entries.size() - 1;
    }

    public void forward() {
        if (canGoForward()) {
            currentIndex++;
        }
    }
}
