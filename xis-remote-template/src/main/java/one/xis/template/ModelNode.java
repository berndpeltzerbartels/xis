package one.xis.template;

public interface ModelNode {

    void accept(WidgetModelVisitor visitor);
}
