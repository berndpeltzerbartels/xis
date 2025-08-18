// Datei: xis-integration-tests/src/main/java/test/page/forms/validation/msg/AllFormElementsModel.java
package test.page.forms.validation.msg;

import lombok.Data;
import one.xis.validation.Mandatory;

import java.util.List;

@Data
public class AllFormElementsModel {
    @Mandatory
    private String textField;

    @Mandatory
    private String textareaField;

    @Mandatory
    private Boolean checkBox;

    @Mandatory
    private String radioField;

    @Mandatory
    private String selectField;

    @Mandatory
    private List<String> listField;

    @Mandatory
    private String[] arrayField;
}