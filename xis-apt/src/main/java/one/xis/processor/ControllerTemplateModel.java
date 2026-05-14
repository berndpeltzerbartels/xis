package one.xis.processor;

import java.nio.file.Path;
import java.util.Set;

record ControllerTemplateModel(String controllerName, Path templateFile, Set<String> modelDataNames, Set<String> formDataNames) {

    boolean providesData(String name) {
        return modelDataNames.contains(name) || formDataNames.contains(name);
    }
}
