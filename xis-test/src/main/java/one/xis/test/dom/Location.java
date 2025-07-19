package one.xis.test.dom;

import lombok.Data;

@Data
public class Location {
    public String pathname;
    public String href;

    void reset() {
        pathname = null;
    }
}
