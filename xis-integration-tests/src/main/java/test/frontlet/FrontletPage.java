package test.frontlet;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/frontletPage.html")
@Setter
@RequiredArgsConstructor
class FrontletPage {
    private String frontletId;

    @ModelData("frontletId")
    String frontletId() {
        return frontletId;
    }
}
