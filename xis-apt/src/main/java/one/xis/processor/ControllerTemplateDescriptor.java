package one.xis.processor;

import java.util.List;

record ControllerTemplateDescriptor(List<TemplateModelData> modelData,
                                    List<TemplateFormData> formData,
                                    List<TemplateAction> actions) {
}
