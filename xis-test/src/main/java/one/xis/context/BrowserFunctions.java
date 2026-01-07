package one.xis.context;

import one.xis.test.js.Array;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class BrowserFunctions {

    public static final BiConsumer<String, Object> debugFunction = BrowserFunctions::debug;
    public static final Function<Object, Boolean> isFloatFunction = BrowserFunctions::isFloat;
    public static final Function<Object, Boolean> isIntFunction = BrowserFunctions::isInt;
    public static final Function<Object, Object> parseFloatFunction = BrowserFunctions::parseFloat;
    public static final BiFunction<Object, Object, Object> parseIntFunction = BrowserFunctions::parseInt;
    public static final Function<Object, Boolean> isNaNFunction = BrowserFunctions::isNaN;

    public static void debug(String text, Object args) {
        System.out.printf("DEBUG: " + text + "\n", args);
    }

    public static boolean isFloat(Object obj) {
        if (obj == null) {
            return false;
        }
        var val = obj;
        if (obj instanceof List<?> list) {
            val = list.get(0);
        }
        if (val instanceof Number) {
            double value = ((Number) val).doubleValue();
            return !Double.isNaN(value) && !Double.isInfinite(value);
        }
        return false;
    }

    public static boolean isInt(Object obj) {
        if (obj == null) {
            return false;
        }
        var val = obj;
        if (obj instanceof List<?> list) {
            val = list.get(0);
        }
        if (val instanceof Number number) {
            double value = number.doubleValue();
            return !Double.isNaN(value) && !Double.isInfinite(value) && value == Math.floor(value);
        }
        return false;
    }

    public static Object parseFloat(Object obj) {
        if (obj == null) {
            return NaN;
        }
        var val = obj;
        if (obj instanceof List<?> list) {
            val = list.get(0);
        }
        if (val instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return NaN;
        }
    }

    public static Object parseInt(Object obj, Object radix) {
        if (obj == null) {
            return NaN;
        }
        var val = obj;
        if (obj instanceof List<?> list) {
            val = list.get(0);
        }
        if (val instanceof Number number) {
            return number.intValue();
        }
        try {
            // radix parameter is ignored as Java's Integer.parseInt always uses decimal by default
            // In JavaScript, radix 10 is standard for decimal parsing
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return NaN;
        }
    }

    public static boolean isNaN(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof List<?> list) {
            var val = list.get(0);
            return val == NaN;
        }
        return obj == NaN;
    }


    public static final Map<String, Object> BINDINGS = Map.of(
            "Array", new Array(),
            "isFloat", isFloatFunction,
            "isInt", isIntFunction,
            "isNaN", isNaNFunction,
            "parseFloat", parseFloatFunction,
            "parseInt", parseIntFunction,
            "debug", debugFunction
    );

    static final Object NaN = new Object();
}
