package test.widget.action;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet("ActionFormWidget")
@RequiredArgsConstructor
public class ActionFormFrontlet {

    @ModelData("form")
    public ActionFormModel getModel() {
        return new ActionFormModel();
    }

    @Action("submit")
    public Class<?> submit(@FormData("form") ActionFormModel model) {
        return switch (model.getValue()) {
            case "1" -> // Stay here
                    null;
            case "2" -> // Another Widget
                    AnotherFrontlet.class;
            case "3" -> // Another Page
                    AnotherPage.class;
            default -> null;
        };
    }
}