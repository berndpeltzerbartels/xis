package one.xis.gradle;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
class JsContentAnalyzer {

    private final String content;
    private final Set<String> declaredClasses = new HashSet<>();
    private final Set<String> superClasses = new HashSet<>();

    void analyze() {
        var tokens = new Tokens(content);
        String match;
        while ((match = tokens.findNext("class", "extends")) != null) {
            if (match.equals("class")) {
                var className = tokens.next();
                if (className != null) {
                    declaredClasses.add(className);
                }
            } else if (match.equals("extends")) {
                var superClassName = tokens.next();
                if (superClassName != null) {
                    superClasses.add(superClassName);
                }
            }
        }
    }


    class Tokens {
        private final List<String> elements;
        int index;

        Tokens(String content) {
            this.elements = List.of(content.split("[\\s\\{]"));
        }

        String next() {
            return index < elements.size() ? elements.get(index) : null;
        }

        String findNext(String... searchValues) {
            var searchSet = Set.of(searchValues);
            while (index < elements.size()) {
                var candidate = elements.get(index++);
                if (searchSet.contains(candidate)) {
                    return candidate;
                }
            }
            return null;
        }


    }
}
