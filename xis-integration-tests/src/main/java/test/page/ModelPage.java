package test.page;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;

@Page("/model.html")
@RequiredArgsConstructor
class ModelPage {
    private final ModelService modelService;

    @ModelData("model")
    Model model(@ModelData("model") Model model) {
        modelService.updateModel(model); // This is not reality-like, but good enough for testing
        return model;
    }

}
