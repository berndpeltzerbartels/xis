package test.page.forms.checkbox;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckboxListFormService {

    private CheckboxListFormModel model = new CheckboxListFormModel();

    public CheckboxListFormModel getModel() {
        return model;
    }

    public void saveModel(CheckboxListFormModel model) {
        this.model = model;
    }
}