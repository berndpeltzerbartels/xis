package one.xis.js;

import lombok.Data;

import java.util.List;

@Data
public class XISWidget {
    private final String name;
    private List<XISObject> childTypes;
}
