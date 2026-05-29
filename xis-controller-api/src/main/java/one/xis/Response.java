package one.xis;

/**
 * Marker interface for controller action results that instruct XIS to perform
 * navigation or UI updates after the action completed.
 *
 * <p>Most application code returns one of the concrete response types such as
 * {@link PageResponse}, {@link PageUrlResponse}, {@link FrontletResponse}, or
 * {@link ModalResponse}. A controller action may also return {@code void} when
 * no explicit navigation response is needed.</p>
 */
public interface Response {

    /**
     * Returns the controller class targeted by this response, if the response is
     * class-based. URL-based responses may return {@code null}.
     */
    Class<?> getControllerClass();
}
