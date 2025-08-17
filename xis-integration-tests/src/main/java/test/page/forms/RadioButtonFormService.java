package test.page.forms;

public class RadioButtonFormService {

    private RadioButtonFormModel model = new RadioButtonFormModel();

    public RadioButtonFormModel getModel() {
        return model;
    }

    public void setModel(RadioButtonFormModel model) {
        this.model = model;
    }

    public void saveModel(RadioButtonFormModel model) {
        this.model = model;
    }
}