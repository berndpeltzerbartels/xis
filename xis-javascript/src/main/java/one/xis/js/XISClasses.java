package one.xis.js;

public class XISClasses {

    public static final JSClass XIS_ROOT = new JSClass("XISRoot"); // TODO check javascript
    public static final JSClass XIS_ELEMENT = new JSClass("XISElement");
    public static final JSClass XIS_LOOP_ELEMENT = new JSClass("XISLoopElement");
    public static final JSClass XIS_CONTAINER = new JSClass("XISContainer");
    public static final JSClass XIS_STATIC_TEXT_NODE = new JSClass("XISStaticTextNode");
    public static final JSClass XIS_MUTABLE_TEXT_NODE = new JSClass("XISMutableTextNode");
    public static final JSClass XIS_LOOP = new JSClass("XISLoop");


    static {

        XIS_ROOT.addMethod("init", 1)
                .addMethod("update", 1)
                .addAbstractMethod("createElement", 0)
                .addMethod("getValue", 1);
        /*

         */
        XIS_ELEMENT.addMethod("init", 1)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes", 0)
                .addMethod("evalIf", 0)
                .addAbstractMethod("createElement", 0)
                .addAbstractMethod("createChildren", 0)
                .addMethod("unlink", 0);

        XIS_LOOP_ELEMENT.addMethod("init", 1)
                .addAbstractMethod("getLoopAttributes", 0)
                .addAbstractMethod("createElement", 0)
                .addAbstractMethod("creaetChildren", 0)
                .addMethod("unlink", 0)
                .addMethod("update", 0)
                .addMethod("updateAllChildren", 0)
                .addMethod("unlinkAll", 0)
                .addAbstractMethod("updateAttributes", 0)
                .addMethod("getValue", 1)
                .addMethod("getArray", 0)
                .addMethod("resize", 1)
                .addMethod("rowCount", 0)
                .addMethod("appendRow", 0)
                .addMethod("removeRow", 0)
                .addMethod("evalIf", 0);

        XIS_CONTAINER.addMethod("init", 1)
                .addMethod("setWidget", 1)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addAbstractMethod("updateAttributes", 0)
                .addMethod("evalIf", 0)
                .addAbstractMethod("createElement", 0)
                .addMethod("unlink", 0);

        XIS_STATIC_TEXT_NODE.addMethod("init", 1)
                .addMethod("update", 1)
                .addAbstractMethod("getText", 0);

        XIS_MUTABLE_TEXT_NODE.addMethod("init", 1)
                .addMethod("update", 1)
                .addAbstractMethod("getText", 0);

    }

}
