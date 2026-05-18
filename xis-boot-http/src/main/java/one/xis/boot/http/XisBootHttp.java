package one.xis.boot.http;

/**
 * Marker type for the {@code xis-boot-http} convenience module.
 * <p>
 * Applications normally do not use this class directly. The module exists so a standalone XIS Boot HTTP application can
 * depend on one artifact instead of declaring {@code xis-boot} and {@code xis-http-controller} separately.
 */
public final class XisBootHttp {

    private XisBootHttp() {
    }
}
