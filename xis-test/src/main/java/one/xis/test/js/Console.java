package one.xis.test.js;


import com.oracle.truffle.js.runtime.builtins.JSErrorObject;

public class Console {

    public void log(String message) {
        System.out.println(message);
    }

    public void log(Object message) {
        System.out.println(message);
    }

    public void log(Object message, Object arg) {
        System.out.println(message);
    }

    public void error(Object message) {
        try {
            printError(message);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println(message);
        }
    }

    public void error(Object message, Object arg) {
        try {
            printError(message);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println(message);
        }
    }

    public void warn(Object message) {
        System.err.println(message);
    }

    public void warn(Object message, Object arg) {
        System.err.println(message);
    }

    public void debug(Object message) {
        System.out.println(message);
    }

    public void debug(Object message, Object arg) {
        System.out.println(message);
    }


    private void printError(Object message) throws NoSuchFieldException, IllegalAccessException {
        var guestObjectField = message.getClass().getDeclaredField("guestObject");
        guestObjectField.setAccessible(true);
        var errorObject = (JSErrorObject) guestObjectField.get(message);
        var exception = errorObject.getException();
        System.err.println(exception.getMessage());
        for (var stackTraceElement : exception.getJSStackTrace()) {
            System.err.println(stackTraceElement);
        }
        for (var stackTraceElement : exception.getStackTrace()) {
            System.err.println(stackTraceElement);
        }
    }

}
