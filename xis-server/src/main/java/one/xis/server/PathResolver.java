package one.xis.server;

import one.xis.Page;
import one.xis.context.XISComponent;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

@XISComponent
class PathResolver {

    Path create(String path) {
        return new Path(evaluate(new StringCharacterIterator(path)));
    }

    String normalizedPath(Object controller) {
        var path = create(controller.getClass().getAnnotation(Page.class).value());
        return path.normalized();
    }

    private PathElement evaluate(CharacterIterator path) {
        PathElement rv = null;
        if (path.current() != CharacterIterator.DONE) {
            var pathString = PathString.read(path);
            if (pathString != null) {
                rv = pathString;
                evaluatePathVariable(path, pathString);
            } else {
                var pathVariable = PathVariable.read(path);
                if (pathVariable != null) {
                    rv = pathVariable;
                    evaluatePathString(path, pathVariable);
                }
            }
        }
        return rv;
    }

    private void evaluatePathString(CharacterIterator path, PathVariable previous) {
        var pathString = PathString.read(path);
        if (pathString != null) {
            previous.setNext(pathString);
            evaluatePathVariable(path, pathString);
        }

    }

    private void evaluatePathVariable(CharacterIterator path, PathString previous) {
        var pathVariable = PathVariable.read(path);
        if (pathVariable != null) {
            previous.setNext(pathVariable);
            evaluatePathString(path, pathVariable);
        }

    }
}
