package one.xis.remote.processor;

import lombok.Data;

import java.io.File;

@Data
class TemplateContext {
    private final String packageName;
    private final File htmlFile;

}
