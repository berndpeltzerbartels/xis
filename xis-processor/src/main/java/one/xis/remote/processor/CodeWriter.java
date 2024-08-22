package one.xis.remote.processor;

import javax.lang.model.element.Element;
import java.io.PrintWriter;

public interface CodeWriter<E extends Element> { // TODO remove module

    void init(E element) throws Exception;

    void writeJavascript(PrintWriter writer) throws Exception;
}
