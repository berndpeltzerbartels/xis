package one.xis.test.dom;

import lombok.Data;

@Data
public class Location {
    public String pathname;


    void reset() {
        pathname = null;
    }
}
