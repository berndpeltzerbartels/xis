package test.frontlet.action;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;

@Page("/actionFormPage.html")
@RequiredArgsConstructor
public class ActionFormPage {
    @ModelData("frontletId")
    public String frontletId() {
        return "ActionFormFrontlet";
    }
}