package one.xis.processor;

import java.util.List;

record PropertyPathUsage(String root, List<String> properties, int line, boolean localVariable) {

    String path() {
        return root + "." + String.join(".", properties);
    }
}
