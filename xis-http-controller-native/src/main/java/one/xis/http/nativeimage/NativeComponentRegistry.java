package one.xis.http.nativeimage;

import java.util.Collection;

/**
 * Build-time generated component catalog for XIS HTTP Controller native-image startup.
 * <p>
 * The normal XIS context can discover components by package scanning. Native
 * images need closed-world friendly class references instead, so generated
 * native catalogs expose the component classes through this interface.
 */
public interface NativeComponentRegistry {

    Collection<Class<?>> componentClasses();
}
