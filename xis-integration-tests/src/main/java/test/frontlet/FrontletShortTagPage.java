package test.frontlet;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/frontletShortTagPage.html")
@Setter
@RequiredArgsConstructor
class FrontletShortTagPage {
    private String frontletId;

    @ModelData("frontletId")
    String frontletId() {
        return frontletId;
    }
}
