package one.xis.server;

import one.xis.html.HtmlParser;
import one.xis.html.document.HtmlDocument;
import one.xis.resource.Resource;

class HtmlDocumentResource implements Resource {
    private final Resource source;
    private final HtmlParser htmlParser;
    private final Class<?> controllerClass;
    private final String type;
    private final String resourcePath;
    private volatile HtmlDocument document;
    private volatile long lastModified;

    HtmlDocumentResource(Resource source, HtmlParser htmlParser, Class<?> controllerClass, String type) {
        this.source = source;
        this.htmlParser = htmlParser;
        this.controllerClass = controllerClass;
        this.type = type;
        this.resourcePath = source.getResourcePath();
        reload();
    }

    HtmlDocumentResource(HtmlDocument document, String resourcePath) {
        this.source = null;
        this.htmlParser = null;
        this.controllerClass = null;
        this.type = null;
        this.resourcePath = resourcePath;
        this.document = document;
    }

    HtmlDocument getObjectContent() {
        if (source != null && source.isObsolete()) {
            reload();
        }
        return document;
    }

    @Override
    public int getLength() {
        return getContent().length();
    }

    @Override
    public String getContent() {
        return getObjectContent().toHtml();
    }

    @Override
    public long getLastModified() {
        getObjectContent();
        return lastModified;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public boolean isObsolete() {
        return source != null && source.isObsolete();
    }

    private synchronized void reload() {
        if (source == null) {
            return;
        }
        if (document != null && !source.isObsolete()) {
            return;
        }
        try {
            document = htmlParser.parse(source.getContent());
            lastModified = source.getLastModified();
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("%s template parsing failed: %s (%s)\n  Controller: %s\n  Path: %s",
                            type,
                            e.getMessage(),
                            e.getClass().getSimpleName(),
                            controllerClass.getName(),
                            source.getResourcePath()),
                    e
            );
        }
    }
}
