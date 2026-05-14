package one.xis.processor;

import java.nio.file.Path;
import java.util.List;

class ValidationErrorCollector {

    private final boolean failFast;
    private final Path projectDir;
    private final Path templateFile;
    private final List<ValidationError> errors;

    ValidationErrorCollector(boolean failFast, Path projectDir, Path templateFile, List<ValidationError> errors) {
        this.failFast = failFast;
        this.projectDir = projectDir;
        this.templateFile = templateFile;
        this.errors = errors;
    }

    void add(int line, String message) {
        if (shouldSkip()) {
            return;
        }
        errors.add(ValidationError.inFile(projectDir, templateFile, line, message));
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    boolean shouldStop() {
        return failFast && hasErrors();
    }

    private boolean shouldSkip() {
        return failFast && hasErrors();
    }
}
