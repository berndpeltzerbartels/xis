package one.xis.deserialize;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PostProcessingResults {
    private final List<PostProcessingResult> results = new ArrayList<>();

    public void add(PostProcessingResult result) {
        results.add(result);
    }

    public Set<Class<? extends PostProcessingResult>> getCurrentResultTypes() {
        return results.stream().map(PostProcessingResult::getClass).collect(Collectors.toSet());
    }

    public boolean reject() {
        return results.stream().anyMatch(PostProcessingResult::reject);
    }

    public boolean authenticate() {
        return results.stream().anyMatch(PostProcessingResult::authenticate);
    }

    public <P extends PostProcessingResult> List<P> postProcessingResults(Class<P> type) {
        return results.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

}
