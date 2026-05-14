package one.xis.test.dom;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MyJavaObject implements ProxyObject {
    private String name;
    private boolean active;

    // Standard-Getter und -Setter
    public String getName() {
        System.out.println("Java: getName() called");
        return name;
    }

    public void setName(String name) {
        System.out.println("Java: setName('" + name + "') called");
        this.name = name;
    }

    public boolean isActive() {
        System.out.println("Java: isActive() called");
        return active;
    }

    public void setActive(boolean active) {
        System.out.println("Java: setActive(" + active + ") called");
        this.active = active;
    }

    // --- Implementierung von ProxyObject ---

    @Override
    public void putMember(String key, Value value) {
        String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
        try {
            // Finde die passende Setter-Methode
            Method setter = Arrays.stream(this.getClass().getMethods())
                    .filter(m -> m.getName().equals(setterName) && m.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("Setter for " + key + " not found"));

            // Konvertiere den Wert und rufe die Methode auf
            Object arg = value.as(setter.getParameterTypes()[0]);
            setter.invoke(this, arg);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot set member " + key, e);
        }
    }

    @Override
    public Object getMember(String key) {
        String getterName1 = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
        String getterName2 = "is" + key.substring(0, 1).toUpperCase() + key.substring(1);
        try {
            // Suche nach get... oder is...
            Method getter = Arrays.stream(this.getClass().getMethods())
                    .filter(m -> (m.getName().equals(getterName1) || m.getName().equals(getterName2)) && m.getParameterCount() == 0)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("Getter for " + key + " not found"));
            return getter.invoke(this);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot get member " + key, e);
        }
    }

    @Override
    public boolean hasMember(String key) {
        // Prüft, ob es einen Getter oder Setter für die Eigenschaft gibt
        String getterName1 = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
        String getterName2 = "is" + key.substring(0, 1).toUpperCase() + key.substring(1);
        String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

        return Arrays.stream(this.getClass().getMethods()).anyMatch(m ->
                m.getName().equals(getterName1) || m.getName().equals(getterName2) || m.getName().equals(setterName)
        );
    }

    @Override
    public Object getMemberKeys() {
        // Liefert eine Liste der verfügbaren Eigenschaften (vereinfacht)
        return List.of("name", "active");
    }
}