package one.xis.js;

import lombok.Data;

import java.util.List;

@Data
public class XISElement implements XISObject {
    private final String name;
    private final String tagName;
    private List<XISObject> childTypes;
}
