package one.xis.processor;

import java.util.Map;

record TemplateDataModel(String name, Map<String, TemplateDataModel> fields, TemplateDataModel elementModel) {

    boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    TemplateDataModel field(String fieldName) {
        return fields.get(fieldName);
    }
}
