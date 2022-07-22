package one.xis.jsc;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract class JavascriptComponents<C extends JavascriptComponent> {
    private final Map<String, C> components = new HashMap<>();

    protected abstract C createComponent(Object controller);

    protected abstract String compile(String key, ResourceFile resourceFile, String javascriptClassName);

    @SuppressWarnings("unused")
    protected abstract String createKey(Object controller);

    public C add(Object controller) {
        String key = createKey(controller);
        C component = createComponent(controller);
        compile(key, component);
        if (components.containsKey(key)) {
            throw new DuplicateKeyException(this, key);
        }
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

    public Collection<String> getKeys() {
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
        String javascript = compile(key, component.getHtmlResourceFile(), component.getJavascriptClass());
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
