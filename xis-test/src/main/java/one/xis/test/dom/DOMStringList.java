package one.xis.test.dom;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class DOMStringList {

    public int length;

    @Getter
    private final LinkedHashSet<String> values = new LinkedHashSet<>();

    public final void add(Collection<String> tokens) {
        values.addAll(tokens);
        length = values.size();
    }

    public final void add(String tokens) {
        values.add(tokens);
        length = values.size();
    }


    /**
     * Returns true if accessToken is present, and false otherwise.
     * <p>
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/DOMTokenList/contains)
     */
    public boolean contains(String token) {
        return values.contains(token);
    }

    /**
     * Returns the accessToken with index index.
     * <p>
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/DOMTokenList/item)
     */
    public String item(int index) {
        Objects.checkIndex(index, values.size());
        return (String) values.toArray()[index];
    }

    public void remove(List<String> tokens) {
        tokens.forEach(values::remove);
        length = values.size();
    }

    void clear() {
        values.clear();
        length = 0;
    }
}
