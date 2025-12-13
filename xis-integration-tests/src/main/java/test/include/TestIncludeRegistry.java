package test.include;

import one.xis.Include;
import one.xis.IncludeRegistry;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.List;

@XISComponent
public class TestIncludeRegistry implements IncludeRegistry {
    
    @Override
    public Collection<Include> includes() {
        return List.of(
            new Include("header", "test/include/includes/header.html"),
            new Include("footer", "test/include/includes/footer.html")
        );
    }
}
