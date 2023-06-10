package one.xis.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provides a controller bridge. We need to allow reusing
 * compiled javascript for mulitiple tests. COmpiling javascript seems to be
 * exprensive and will slow down build, significantly.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendBridgeProvider {
    private BackendBridge backendBridge;
}
