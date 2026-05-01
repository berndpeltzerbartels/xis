package one.xis.distributed;

import one.xis.ImportInstances;

/**
 * Configuration interface for distributed / micro-frontend XIS deployments.
 * <p>
 * This interface models only the remote-routing part of a distributed application.
 * Components that are not marked as remote remain same-origin and therefore return
 * {@code null} from the corresponding host lookup.
 * <p>
 * {@link ImportInstances} is attached for internal framework integration so that
 * runtime-specific beans can be imported into the XIS context. It is not part of the
 * public application contract.
 */
@ImportInstances
public interface XisDistributedConfig {

    /**
     * Returns whether the given widget is intentionally routed to a remote server.
     * Local widgets must return {@code false}.
     */
    boolean isRemoteWidget(String widgetId);

    /**
     * Returns whether the given normalised page path is intentionally routed to a
     * remote server. Local pages must return {@code false}.
     */
    boolean isRemotePage(String normalizedPath);

    /**
     * Returns the host (scheme + authority) for the given remote widget-id.
     * Returns {@code null} only for local widgets.
     */
    String getWidgetHost(String widgetId);

    /**
     * Returns the host for the given remote normalised page path.
     * Returns {@code null} only for local pages.
     */
    String getPageHost(String normalizedPath);
}
