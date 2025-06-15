package one.xis.test.dom;

import java.util.ArrayList;
import java.util.List;

public class History {

    private static class Entry {
        Object state;
        String title;
        Object id;

        Entry(Object state, String title, Object id) {
            this.state = state;
            this.title = title;
            this.id = id;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private int currentIndex = -1;

    public void pushState(Object state, String title, Object id) {
        // Entferne alle „zukünftigen“ Einträge
        while (entries.size() > currentIndex + 1) {
            entries.remove(entries.size() - 1);
        }
        entries.add(new Entry(state, title, id));
        currentIndex++;
    }

    public void pushState(Object state, String title) {
        pushState(state, title, null); // ID optional
    }

    public void replaceState(Object state, String title, Object id) {
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

    public Object getCurrentState() {
        if (currentIndex >= 0 && currentIndex < entries.size()) {
            return entries.get(currentIndex).state;
        }
        return null;
    }

    public Object getCurrentId() {
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
