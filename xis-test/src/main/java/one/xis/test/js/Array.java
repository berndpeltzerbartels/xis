package one.xis.test.js;

/**
 * Imitates the JavaScript Array object.
 */
public class Array {

    public boolean isArray(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Array) {
            return true;
        }
        if (value instanceof Object[]) {
            return true;
        }
        if (value instanceof Iterable) {
            return true;
        }
        return false;
    }
}
