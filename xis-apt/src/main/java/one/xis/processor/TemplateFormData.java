package one.xis.processor;

import java.util.List;

record TemplateFormData(String name, String actionName, List<TemplateField> fields) {
}
