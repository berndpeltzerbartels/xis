package test.page.forms.textarea;

import lombok.Data;
import one.xis.validation.Mandatory;

@Data
public class TextareaFormModel {
    @Mandatory
    private String text;
}