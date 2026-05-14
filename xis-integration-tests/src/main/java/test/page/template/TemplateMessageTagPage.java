package test.page.template;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.validation.Mandatory;

@Page("/template-message-tag.html")
class TemplateMessageTagPage {

    @FormData("formData")
    FormModel formData() {
        return new FormModel();
    }

    @Action("save")
    void save(@FormData("formData") FormModel formData) {
    }

    static class FormModel {
        @Mandatory
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
