package one.xis.jscomponent;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;

import java.util.HashMap;
import java.util.Map;

abstract class JavascriptComponents<C extends JavascriptComponent> {
    private final Map<String, C> components = new HashMap<>();

    protected abstract C createComponent(Object controller);

    protected abstract String compile(String className, ResourceFile resourceFile);

    public C add(String clazz, Object controller) {
        C component = createComponent(controller);
        compile(clazz, component);
        components.put(clazz, component);
        return component;
    }

    public C get(String urn) {
        String componentClass = JavasscriptComponentUtils.urnToClassName(urn);
        C component = components.get(componentClass);
        if (component == null) {
            throw new IllegalStateException("no such element: " + urn);
        }
        return compileIfObsolete(componentClass, component);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private C compileIfObsolete(String componentClassName, C component) {
        synchronized (component) {
            if (isObsolete(component) || !component.isCompiled()) {
                compile(componentClassName, component);
            }
            return component;
        }
    }

    private void compile(String componentClassName, C component) {
        component.setCompiled(false);
        reloadHtml(component);
        String javascript = compile(componentClassName, component.getHtmlResourceFile());
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
