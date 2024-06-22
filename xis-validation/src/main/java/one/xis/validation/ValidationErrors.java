package one.xis.validation;

import lombok.Getter;

import java.util.*;

@Getter
public class ValidationErrors {
    private final Set<String> errorPaths = new HashSet<>();
    private final Map<String, String> errors = new HashMap<>();
    private final Collection<String> globalErrors = new HashSet<>();


    public void addError(String path) {
        errorPaths.add(path);
    }

    public void addError(String path, String message) {
        if (errorPaths.contains(path)) {
            return;
        }
        errors.put(path, message);
        errorPaths.add(path);
    }

    public void addGlobalError(String message) {
        globalErrors.add(message);
    }

    public boolean hasErrors() {
        return !errorPaths.isEmpty();
    }

    public boolean hasError(String path) {
        return errorPaths.contains(path);
    }

    public boolean isEmpty() {
        return errorPaths.isEmpty();
    }

    public String getError(String path) {
        return errors.get(path);
    }

}
