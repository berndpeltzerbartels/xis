package one.xis.context.singletonwithcollection;

public class MyImplementation implements MyInterface {
    private final String name;

    public MyImplementation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
