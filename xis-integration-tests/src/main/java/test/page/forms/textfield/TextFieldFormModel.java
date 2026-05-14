package test.page.forms.textfield;

import lombok.Data;
import one.xis.validation.Mandatory;

@Data
public class TextFieldFormModel {
    @Mandatory
    private Integer number;
}