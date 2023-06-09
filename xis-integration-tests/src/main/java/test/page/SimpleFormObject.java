package test.page;

import lombok.Data;
import one.xis.Identifier;

@Data
class SimpleFormObject {

    @Identifier
    private int id;
    private String property1;
    private String property2;
}
