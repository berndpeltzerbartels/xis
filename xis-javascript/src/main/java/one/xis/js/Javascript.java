package one.xis.js;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Javascript {

    public static String getScript(JavascriptSource... sources) {
        return Arrays.stream(sources).map(JavascriptSource::getContent).collect(Collectors.joining("\n")) + "\n";
    }
}
