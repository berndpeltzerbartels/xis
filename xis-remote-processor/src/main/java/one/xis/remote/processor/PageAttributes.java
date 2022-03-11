package one.xis.remote.processor;

import lombok.Getter;

import java.io.File;


@Getter
class PageAttributes extends WidgetAttributes {
    private final String httpPath;

    public PageAttributes(String packageName, String simpleClassName, File htmlFile, String httpPath) {
        super(packageName, simpleClassName, htmlFile);
        this.httpPath = httpPath;
    }
}
