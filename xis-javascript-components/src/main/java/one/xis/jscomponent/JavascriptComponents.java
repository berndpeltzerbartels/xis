package one.xis.jscomponent;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract class JavascriptComponents<C extends JavascriptComponent> {
    private final Map<String, C> components = new HashMap<>();

    protected abstract C createComponent(Object controller);

    protected abstract String compile(String name, ResourceFile resourceFile);

    @SuppressWarnings("unused")
    protected String createKey(String name, Object controller) {
        return name;
    }

    public C add(String name, Object controller) {
        String key = createKey(name, controller);
        C component = createComponent(controller);
        compile(key, component);
        components.put(key, component);
        return component;
    }

    public C get(String key) {
        C component = components.get(key);
        if (component == null) {
            throw new IllegalStateException("no such element: " + key);
        }
        return compileIfObsolete(key, component);
    }

    public Map<String, C> getAll() {
        return components;
    }

    public Collection<String> getNames() {
        return components.keySet();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private C compileIfObsolete(String key, C component) {
        synchronized (component) {
            if (isObsolete(component) || !component.isCompiled()) {
                compile(key, component);
            }
            return component;
        }
    }

    private void compile(String key, C component) {
        component.setCompiled(false);
        reloadHtml(component);
        String javascript = compile(key, component.getHtmlResourceFile());
        component.setJavascript(javascript);
        component.setCompiled(true);
    }

    private void reloadHtml(C component) {
        if (component.getHtmlResourceFile() instanceof ReloadableResourceFile) {
            ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) component.getHtmlResourceFile();
            reloadableResourceFile.reload();
        } else {
            throw new IllegalStateException();
        }
    }

    private boolean isObsolete(C component) {
        if (component.getHtmlResourceFile() instanceof ReloadableResourceFile) {
            ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) component.getHtmlResourceFile();
            return reloadableResourceFile.isObsolete();
        }
        return false;
    }

}
