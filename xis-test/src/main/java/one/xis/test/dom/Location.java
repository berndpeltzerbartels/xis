package one.xis.test.dom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    public String pathname;
    public String href;
    public String origin;
    public String search;

    public Location(String pathname, String href) {
        this.pathname = pathname;
        this.href = href;
    }

    void reset() {
        pathname = null;
        href = null;
        origin = null;
        search = null;
    }
}
