package one.xis.remote.processor;

import lombok.Value;

import java.io.File;


@Value
class PageAttributes implements TemplateAttributes {
    String htmlFilePath;
    String httpPath;
    File htmlFile;

}
