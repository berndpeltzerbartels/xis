package one.xis.processor;

import javax.lang.model.element.Element;
import java.io.PrintWriter;

public interface CodeWriter<E extends Element> {

    void init(E element) throws Exception;

    void writeJavascript(PrintWriter writer) throws Exception;
}
