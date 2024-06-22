package one.xis.validation;

public class RootPathElement extends DefaultPathElement {

    public RootPathElement() {
        super("", "");
    }

    @Override
    public ArrayPathElement addArrayChild() {
        var pathElement = new ArrayPathElement(this);
        getChildren().put(pathElement.getName(), pathElement);
        return pathElement;
    }
    /*
    @Override
    public DefaultPathElement addChild(String name) {
        if (getChildren().containsKey(name)) {
            throw new IllegalArgumentException("Child with name " + name + " already exists");
        }
        var pathElement = new DefaultPathElement(name, getPath() + "/" + name + "[0]");
        getChildren().put(name, pathElement);
        return pathElement;
    }

     */

}
