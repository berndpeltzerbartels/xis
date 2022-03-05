package one.xis.js;

public class Classes {

    public static final JSSuperClass XIS_ROOT = new JSSuperClass("XISRoot"); // TODO check javascript
    public static final JSSuperClass XIS_ELEMENT = new JSSuperClass("XISElement");
    public static final JSSuperClass XIS_CONTAINER = new JSSuperClass("XISContainer");
    public static final JSSuperClass XIS_STATIC_TEXT_NODE = new JSSuperClass("XISStaticTextNode");
    public static final JSSuperClass XIS_MUTABLE_TEXT_NODE = new JSSuperClass("XISMutableTextNode");
    public static final JSSuperClass XIS_WIDGETS = new JSSuperClass("XISWidgets");
    public static final JSSuperClass XIS_LOOP = new JSSuperClass("XISLoop");
    public static final JSSuperClass XIS_IF = new JSSuperClass("XISIf");

    static {

        XIS_IF.addMethod("init", 2)
                .addMethod("update", 0)
                .addAbstractMethod("evaluateCondition")
                .addMethod("initChildren", 0)
                .addMethod("unlinkChildren", 0);

        XIS_LOOP.addMethod("init", 2)
                .addMethod("update", 0)
                .addMethod("updateChildren", 0)
                .addMethod("getValue", 0)
                .addAbstractMethod("getArray")
                .addMethod("resize", 1)
                .addMethod("rowCount", 0)
                .addMethod("appendRow", 0)
                .addMethod("removeRow", 0);

        XIS_ROOT.addMethod("init", 1)
                .addMethod("update", 1)
                .addMethod("initChildren", 0)
                .addMethod("getValue", 1);

        XIS_ELEMENT.addMethod("init", 2)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0);

        XIS_CONTAINER.addMethod("init", 2)
                .addMethod("setWidget", 1)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0);

        XIS_STATIC_TEXT_NODE.addMethod("init", 2)
                .addMethod("update", 0);

        XIS_MUTABLE_TEXT_NODE.addMethod("init", 1) // ONE ARG HERE !
                .addMethod("update", 0)
                .addAbstractMethod("getText")
                .addMethod("getValue", 1);

        XIS_WIDGETS.addMethod("getWidget", 1)
                .addMethod("bind", 2);
    }

}
