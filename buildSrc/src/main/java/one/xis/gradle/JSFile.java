package one.xis.gradle;

import lombok.Data;

import java.io.File;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JSFile jsFile = (JSFile) o;
        return file.equals(jsFile.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
