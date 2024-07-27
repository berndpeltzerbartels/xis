package test.page;

import lombok.Data;
import one.xis.validation.NotEmpty;

@Data
class IncompatibleParameterPageData {

    @NotEmpty
    private Integer integerFieldMandatory;

    private Integer integerField;
}
