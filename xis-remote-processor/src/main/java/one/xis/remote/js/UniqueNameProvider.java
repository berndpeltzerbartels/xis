package one.xis.remote.js;

class UniqueNameProvider {
    private static int current;

    static synchronized String nextName(String prefix) {
        return prefix + (++current);
    }

    static String nextName() {
        return nextName("x");
    }

}
