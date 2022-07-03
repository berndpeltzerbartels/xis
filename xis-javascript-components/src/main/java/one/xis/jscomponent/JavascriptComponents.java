package one.xis.jscomponent;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static one.xis.jscomponent.JavasscriptComponentUtils.urnToClassName;

abstract class JavascriptComponents<C extends JavascriptComponent> {
    private final Map<String, C> components = new HashMap<>();

    protected abstract C createComponent(Object controller);

    protected abstract String compile(String name, ResourceFile resourceFile);

    public C add(String name, Object controller) {
        C component = createComponent(controller);
        compile(name, component);
        components.put(name, component);
        return component;
    }

    public C get(String urn) {
        String name = urnToClassName(urn);
        C component = components.get(name);
        if (component == null) {
            throw new IllegalStateException("no such element: " + urn);
        }
        return compileIfObsolete(name, component);
    }

    public Collection<String> getNames() {
        return components.keySet();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private C compileIfObsolete(String name, C component) {
        synchronized (component) {
            if (isObsolete(component) || !component.isCompiled()) {
                compile(name, component);
            }
            return component;
        }
    }

    private void compile(String name, C component) {
        component.setCompiled(false);
        reloadHtml(component);
        String javascript = compile(name, component.getHtmlResourceFile());
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
