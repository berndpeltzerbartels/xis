package one.xis.deserialize;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostProcessingObjects {
    private final List<PostProcessingObject> postProcessingObjects = new ArrayList<>();

    public void add(PostProcessingObject result) {
        postProcessingObjects.add(result);
    }

    public boolean reject() {
        return postProcessingObjects.stream().anyMatch(PostProcessingObject::reject);
    }

    public <P extends PostProcessingObject> List<P> postProcessingObjects(Class<P> type) {
        return postProcessingObjects.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

}
