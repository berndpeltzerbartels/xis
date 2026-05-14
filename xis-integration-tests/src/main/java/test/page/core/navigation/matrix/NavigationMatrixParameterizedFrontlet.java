package test.page.core.navigation.matrix;

import one.xis.Frontlet;
import one.xis.Parameter;
import one.xis.ModelData;

import java.util.Map;

@Frontlet(url = "/navigation/parameterized-frontlet", containerId = "main")
class NavigationMatrixParameterizedFrontlet {

    @ModelData
    String message(@Parameter("message") String message) {
        return message;
    }

    @ModelData
    String allParameters(@Parameter Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }
}
