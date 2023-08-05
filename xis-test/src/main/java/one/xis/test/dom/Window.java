package one.xis.test.dom;

public class Window {
    public Location location = new Location();
    public History history = new History();


    public void reset() {
        location.reset();
        history.reset();
    }
}
