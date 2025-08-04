package one.xis.context;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class UnresolvedDependencyAnalyzer {

    private final List<SingletonProducer> uninvokedProducers;
    private final Map<Class<?>, SingletonProducer> producersByClass;

    public UnresolvedDependencyAnalyzer(List<SingletonProducer> uninvokedProducers, Collection<SingletonProducer> allProducers) {
        this.uninvokedProducers = uninvokedProducers;
        this.producersByClass = allProducers.stream()
                .collect(Collectors.toMap(SingletonProducer::getSingletonClass, Function.identity(), (p1, p2) -> p1));
    }

    public String analyze() {
        if (uninvokedProducers.isEmpty()) {
            return "";
        }

        StringBuilder errorMessage = new StringBuilder("Could not create all beans. Failure reasons:");
        Set<SingletonProducer> analyzed = new HashSet<>();

        for (SingletonProducer producer : uninvokedProducers) {
            if (analyzed.contains(producer)) {
                continue;
            }
            findCycleOrMissingBean(producer, new ArrayList<>(), analyzed, errorMessage);
        }

        return errorMessage.toString();
    }

    private void findCycleOrMissingBean(SingletonProducer current, List<SingletonProducer> path, Set<SingletonProducer> analyzed, StringBuilder errorMessage) {
        path.add(current);
        analyzed.add(current);

        for (SingletonConsumer dependency : getDependencies(current)) {
            SingletonProducer nextProducer = producersByClass.get(dependency.getConsumedClass());

            if (nextProducer == null) {
                reportMissingBean(current, dependency, errorMessage);
                return;
            }

            int cycleStartIndex = path.indexOf(nextProducer);
            if (cycleStartIndex != -1) {
                reportCycle(path, cycleStartIndex, errorMessage);
                // Mark all producers in the cycle as analyzed to avoid redundant reports.
                path.subList(cycleStartIndex, path.size()).forEach(analyzed::add);
                return;
            }

            if (!analyzed.contains(nextProducer)) {
                findCycleOrMissingBean(nextProducer, path, analyzed, errorMessage);
            }
        }
        path.remove(path.size() - 1);
    }

    private void reportCycle(List<SingletonProducer> path, int cycleStartIndex, StringBuilder errorMessage) {
        errorMessage.append("\n\n--- Circular Dependency Found ---\n");
        List<SingletonProducer> cyclePath = path.subList(cycleStartIndex, path.size());
        for (int i = 0; i < cyclePath.size(); i++) {
            SingletonProducer from = cyclePath.get(i);
            SingletonProducer to = cyclePath.get((i + 1) % cyclePath.size());
            errorMessage.append("  Bean '").append(from.getSingletonClass().getName()).append("'\n")
                    .append("  -> depends on '").append(to.getSingletonClass().getName()).append("'\n");
        }
        errorMessage.append("---------------------------------");
    }

    private void reportMissingBean(SingletonProducer current, SingletonConsumer dependency, StringBuilder errorMessage) {
        errorMessage.append("\n\n--- Missing Bean ---\n")
                .append("  Bean '").append(current.getSingletonClass().getName()).append("'\n")
                .append("  -> requires bean of type '").append(dependency.getConsumedClass().getName()).append("' which was not found.\n")
                .append("--------------------");
    }

    private List<? extends SingletonConsumer> getDependencies(SingletonProducer producer) {
        if (producer instanceof SingletonConstructor) {
            return ((SingletonConstructor) producer).getParameters();
        }
        if (producer instanceof SingletonMethod) {
            return ((SingletonMethod) producer).getParameters();
        }
        return List.of();
    }
}