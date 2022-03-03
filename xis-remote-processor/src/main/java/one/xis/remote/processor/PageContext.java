package one.xis.remote.processor;


import lombok.Data;

import java.io.File;

@Data
class PageContext {
    private final String urlPattern;
    private final String packageName;
    private final String simpleClassName;
    private final File htmlFile;
}
