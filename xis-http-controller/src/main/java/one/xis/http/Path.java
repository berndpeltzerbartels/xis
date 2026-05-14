package one.xis.http;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Path {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    private final PathElement head;

    Path(String pathTemplate) {
        this.head = parse(pathTemplate);
    }
// In one.xis.server.Path

    public MethodMatchResult matches(String requestPath) {
        Map<String, String> pathVariables = new HashMap<>();
        String remainingPath = requestPath;
        PathElement current = head;

        while (current != null) {
            remainingPath = current.match(remainingPath, pathVariables);
            if (remainingPath == null) {
                return MethodMatchResult.noMatch(); // Kein Match
            }
            current = current.getNext();
        }

        // Stellt sicher, dass der Pfad vollständig konsumiert wurde.
        // Ein verbleibender "/" ist nur ok, wenn der ursprüngliche Pfad damit endete.
        if (!remainingPath.isEmpty() && !(remainingPath.equals("/") && head.normalized().endsWith("/"))) {
            return MethodMatchResult.noMatch();
        }

        return MethodMatchResult.match(pathVariables);
    }

    private PathElement parse(String pathTemplate) {
        if (pathTemplate == null || pathTemplate.isEmpty()) {
            return new StaticPathElement("");
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(pathTemplate);
        PathElement head = null;
        PathElement current = null;
        int lastEnd = 0;

        while (matcher.find()) {
            // Statischer Teil vor der Variable
            if (matcher.start() > lastEnd) {
                String staticPart = pathTemplate.substring(lastEnd, matcher.start());
                PathElement staticElement = new StaticPathElement(staticPart);
                if (head == null) {
                    head = staticElement;
                } else {
                    current.setNext(staticElement);
                }
                current = staticElement;
            }

            // Variabler Teil
            String varName = matcher.group(1);
            PathElement variableElement = new VariablePathElement(varName);
            if (head == null) {
                head = variableElement;
            } else {
                current.setNext(variableElement);
            }
            current = variableElement;

            lastEnd = matcher.end();
        }

        // Verbleibender statischer Teil nach der letzten Variable
        if (lastEnd < pathTemplate.length()) {
            String staticPart = pathTemplate.substring(lastEnd);
            PathElement staticElement = new StaticPathElement(staticPart);
            if (head == null) {
                head = staticElement;
            } else {
                current.setNext(staticElement);
            }
        }
        return head;
    }


    static abstract class PathElement {
        abstract String normalized();

        @Getter
        @Setter
        private PathElement next;

        abstract String evaluate(Map<String, Object> pathVariables);

        /**
         * Versucht, dieses Element am Anfang des angegebenen Pfads abzugleichen.
         *
         * @param path          der zu prüfende Pfad
         * @param pathVariables eine Map zum Sammeln der extrahierten Variablen
         * @return der verbleibende Rest des Pfads nach einem erfolgreichen Match, oder null, wenn es nicht passt.
         */
        abstract String match(String path, Map<String, String> pathVariables);
    }


    @RequiredArgsConstructor
    static class StaticPathElement extends PathElement {
        private final String value;

        @Override
        String normalized() {
            return value;
        }

        @Override
        String evaluate(Map<String, Object> pathVariables) {
            return value;
        }

        @Override
        String match(String path, Map<String, String> pathVariables) {
            if (path.startsWith(value)) {
                return path.substring(value.length());
            }
            return null;
        }
    }


    @RequiredArgsConstructor
    static
    class VariablePathElement extends PathElement {
        private final String name;

        @Override
        String normalized() {
            return "{" + name + "}";
        }

        @Override
        String evaluate(Map<String, Object> pathVariables) {
            return String.valueOf(pathVariables.get(name));
        }


        @Override
        String match(String path, Map<String, String> pathVariables) {
            PathElement next = getNext();
            String lookahead = null;

            // Finde das nächste *statische* Element für den Lookahead
            PathElement temp = next;
            while (temp != null) {
                if (temp instanceof StaticPathElement) {
                    lookahead = temp.normalized();
                    if (!lookahead.isEmpty()) {
                        break;
                    }
                }
                temp = temp.getNext();
            }

            int endIndex;
            if (lookahead != null) {
                endIndex = path.indexOf(lookahead);
                if (endIndex == -1) {
                    return null; // Nächstes statisches Segment nicht gefunden
                }
            } else {
                // Kein statischer Lookahead, nimm alles bis zum nächsten Slash
                endIndex = path.indexOf('/');
                if (endIndex == -1) {
                    endIndex = path.length(); // Oder bis zum Ende, wenn kein Slash
                }
            }

            // Sonderfall für aufeinanderfolgende Variablen: {a}{b}
            if (next instanceof VariablePathElement && lookahead == null) {
                // {a} ist nicht das letzte Element, aber es folgt kein statischer Teil.
                // Das ist mehrdeutig. Wir lassen {a} leer und übergeben den ganzen Pfad an {b}.
                pathVariables.put(name, "");
                return path;
            }

            String variableValue = path.substring(0, endIndex);
            pathVariables.put(name, variableValue);
            return path.substring(endIndex);
        }
    }
}
