package one.xis.plugin;

import lombok.Value;

import java.io.File;

@Value
class ProjectAttributes {
    File sourceFolder;
    String htmlSuffix;
}
