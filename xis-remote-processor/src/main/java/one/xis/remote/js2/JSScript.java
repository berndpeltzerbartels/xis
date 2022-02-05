package one.xis.remote.js2;

import java.util.ArrayList;
import java.util.List;

class JSScript {
    private final List<JSFunctionDeclaration> functions = new ArrayList<>();
    private final List<JSObjectDeclaration> objects = new ArrayList<>();
    private final List<JSFunctionCall> functionCalls = new ArrayList<>();

    JSScript addFunction(JSFunctionDeclaration funct) {
        functions.add(funct);
        return this;
    }

    JSScript addObject(JSObjectDeclaration obj) {
        objects.add(obj);
        return this;
    }

    JSScript addFaunctionCall(JSFunctionCall call) {
        functionCalls.add(call);
        return this;
    }

}
