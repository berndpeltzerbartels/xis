package one.xis.processor;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

record ControllerTemplateModel(String controllerName,
                               Path templateFile,
                               Map<String, TemplateDataModel> modelData,
                               Map<String, TemplateDataModel> formData) {

    boolean providesData(String name) {
        return modelData.containsKey(name) || formData.containsKey(name);
    }

    boolean providesModelData(String name) {
        return modelData.containsKey(name);
    }

    Set<String> modelDataNames() {
        return modelData.keySet();
    }

    TemplateDataModel modelData(String name) {
        return modelData.get(name);
    }

    Set<String> formDataNames() {
        return formData.keySet();
    }

    TemplateDataModel formData(String name) {
        return formData.get(name);
    }
}
