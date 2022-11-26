package one.xis.gradle;

import lombok.Data;

import java.io.File;
import java.util.Set;

@Data
class JSFile {

    private final File file;
    private final String content;
    private final Set<String> declaredClasses;
    private final Set<String> superClasses;

    @Override
    public String toString() {
        return "JSFile{" +
                "file=" + file.getName() +
                '}';
    }
}
