package one.xis.template;

import java.util.Map;

public interface ElementWithAttributes {
    Map<String, MutableAttribute> getMutableAttributes();

    Map<String, String> getStaticAttributes();

    String getElementName();
}
