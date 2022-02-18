package one.xis.template;

public class WidgetModelWalker {

    public void walk(WidgetModel model, WidgetModelVisitor visitor) {
        deepWalk(model, visitor);
    }

    private void deepWalk(ModelNode node, WidgetModelVisitor visitor) {
        if (node instanceof ElementBase) {
            if (node instanceof WidgetModel) {
                visitor.visitModel((WidgetModel) node);
            } else if (node instanceof ModelElement) {
                visitor.visitElement((ModelElement) node);
            } else if (node instanceof ContainerElement) {
                visitor.visitContainer((ContainerElement) node);
            }
        } else if (node instanceof TextNode) {
            if (node instanceof MutableTextNode) {
                visitor.visitMutableTextNode((MutableTextNode) node);
            } else if (node instanceof StaticTextNode) {
                visitor.visitStaticTextNode((StaticTextNode) node);
            }
        }
    }
}
