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

    void reset() {
        pathname = null;
    }
}
