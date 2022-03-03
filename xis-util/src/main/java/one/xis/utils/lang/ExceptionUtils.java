package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.io.PrintWriter;
import java.io.StringWriter;

@UtilityClass
public class ExceptionUtils {

    public String getStackTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
            t.printStackTrace(writer);
        }
        return stringWriter.toString();
    }
}
