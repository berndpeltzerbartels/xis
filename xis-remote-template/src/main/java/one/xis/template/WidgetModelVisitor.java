package one.xis.template;

public interface WidgetModelVisitor {

    default void visitModel(WidgetModel model) {
    }

    default void visitElement(ModelElement element) {
    }

    default void visitContainer(ContainerElement element) {
    }

    default void visitStaticTextNode(StaticTextNode staticTextNode) {
    }

    default void visitMutableTextNode(MutableTextNode mutableTextNode) {
    }
}
