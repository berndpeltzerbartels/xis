package one.xis.test.it;

import lombok.RequiredArgsConstructor;
import one.xis.Page;

@Page("/model.html")
@RequiredArgsConstructor
class ModelPage {
    private final ModelService modelService;

    @one.xis.Model("model")
    Model model() {
        return modelService.getModel();
    }

}
