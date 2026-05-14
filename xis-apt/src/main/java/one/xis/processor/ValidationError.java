package one.xis.processor;

import java.nio.file.Path;

record ValidationError(Path file, int line, String message) {

    static ValidationError inFile(Path projectDir, Path file, int line, String message) {
        Path displayPath = projectDir.relativize(file);
        return new ValidationError(file, line, displayPath + ":" + line + ": " + message);
    }
}
