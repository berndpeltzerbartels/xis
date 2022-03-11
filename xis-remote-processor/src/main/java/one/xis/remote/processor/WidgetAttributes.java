package one.xis.remote.processor;

import lombok.Data;

import java.io.File;

@Data
class WidgetAttributes implements TemplateAttributes {
    private final String packageName;
    private final String simpleClassName;
    private final File htmlFile;
}
