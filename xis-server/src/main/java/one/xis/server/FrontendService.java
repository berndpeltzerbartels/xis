package one.xis.server;

import one.xis.resource.Resource;

/**
 * Internal service used by the runtime adapters to serve the XIS frontend.
 */
public interface FrontendService {

    ClientConfig getConfig();

    ServerResponse processActionRequest(ClientRequest request);

    ServerResponse processModelDataRequest(ClientRequest request);

    ServerResponse processFormDataRequest(ClientRequest request);

    Resource getPageHead(String id);

    Resource getPageBody(String id);

    Resource getBodyAttributes(String id);

    Resource getFrontletHtml(String id);

    Resource getIncludeHtml(String key);

    String getRootPageHtml();

    Resource getBundleJs();

    Resource getBundleJsMap();
}
