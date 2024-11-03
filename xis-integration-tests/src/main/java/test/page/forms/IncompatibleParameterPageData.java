package test.page.forms;

import lombok.Data;
import one.xis.validation.Mandatory;

@Data
class IncompatibleParameterPageData {

    @Mandatory
    private Integer integerFieldMandatory;

    private Integer integerField;
}
