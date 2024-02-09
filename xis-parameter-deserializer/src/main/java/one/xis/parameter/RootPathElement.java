package one.xis.parameter;

class RootPathElement extends PathElement {
    RootPathElement() {
        super(null, 0, null);
    }

    @Override
    protected String elementName() {
        return "";
    }
}
