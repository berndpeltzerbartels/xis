package one.xis.context;

import lombok.Getter;
import one.xis.utils.lang.ClassUtils;

public class MainClassUtil {
    
    @Getter
    private final static boolean runningFromJar = checkRunningFromJar();

    static boolean checkRunningFromJar() {
        var mainClass = ClassUtils.classForName(getMainClassName());
        String path = mainClass.getResource(mainClass.getSimpleName() + ".class").toString();
        return path.startsWith("jar:");
    }

    private static String getMainClassName() {
        StackTraceElement trace[] = Thread.currentThread().getStackTrace();
        if (trace.length > 0) {
            return trace[trace.length - 1].getClassName();
        }
        return null;
    }
}
