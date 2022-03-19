package one.xis.remote.processor;

import lombok.Value;

import java.io.File;

@Value
class WidgetAttributes implements TemplateAttributes {
    String name;
    String htmlFilePath;
    File htmlFile;
}
