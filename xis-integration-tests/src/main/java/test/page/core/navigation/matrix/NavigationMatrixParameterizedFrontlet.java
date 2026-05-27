package test.page.core.navigation.matrix;

import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.ModelData;
import one.xis.ClientState;

import java.util.Map;

@Frontlet(url = "/navigation/parameterized-frontlet", containerId = "main")
class NavigationMatrixParameterizedFrontlet {

    @ModelData
    String message(@FrontletParameter("message") String message) {
        return message;
    }

    @ModelData
    String allParameters(@FrontletParameter Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    @ClientState("frontletNavigationState")
    String navigationState(@FrontletParameter("message") String message) {
        return "state-" + message;
    }

    @ModelData
    String navigationStateEcho(@ClientState("frontletNavigationState") String navigationState) {
        return navigationState;
    }
}
