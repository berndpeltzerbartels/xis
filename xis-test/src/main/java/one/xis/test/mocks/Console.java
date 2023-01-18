package one.xis.test.mocks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Console {

    @Getter
    private final List<Object> infoLog = new ArrayList<>();
    private PrintStream printStream;


    @SuppressWarnings("unused") // ScriptEngine is using it
    public void log(Object o) {
        if (printStream != null) {
            printStream.println(o);
        }
        infoLog.add(o);
    }
}
