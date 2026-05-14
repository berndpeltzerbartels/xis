package test.page.forms.validation.msg;

import one.xis.validation.Mandatory;

import java.util.List;

public record AllFormElementsRecordModel(
    @Mandatory
    String textField,

    @Mandatory
    String textareaField,

    @Mandatory
    Boolean checkBox,

    @Mandatory
    String radioField,

    @Mandatory
    String selectField,

    @Mandatory
    List<String> listField,

    @Mandatory
    String[] arrayField
) {
}
