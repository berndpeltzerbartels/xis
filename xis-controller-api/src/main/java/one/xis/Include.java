package one.xis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an HTML include template that can be embedded in pages and widgets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Include {
    
    /**
     * Unique key to reference this include in templates via &lt;xis:include key="..."/&gt;
     */
    private String key;
    
    /**
     * Path to the HTML template file, relative to the classpath.
     * Example: "my/app/navigation.include.html"
     */
    private String path;
}
