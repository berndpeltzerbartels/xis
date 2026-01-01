package one.xis.html;

import one.xis.html.parts.Part;
import one.xis.html.parts.PartParser;
import one.xis.html.parts.Tag;
import one.xis.html.parts.TagType;
import one.xis.html.tokens.HtmlTokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TemplateValidator {

    public static void main(String[] args) throws IOException {
        Path docsDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("src/main/java");
        
        System.out.println("Scanning for HTML templates in: " + docsDir.toAbsolutePath());
        System.out.println("=".repeat(80));
        
        List<ValidationError> allErrors = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(docsDir)) {
            paths.filter(p -> p.toString().endsWith(".html"))
                 .sorted()
                 .forEach(htmlFile -> {
                     try {
                         System.out.println("\nChecking: " + docsDir.relativize(htmlFile));
                         String html = Files.readString(htmlFile);
                         List<ValidationError> errors = validateTemplate(html, htmlFile);
                         if (errors.isEmpty()) {
                             System.out.println("  ✓ OK");
                         } else {
                             allErrors.addAll(errors);
                             errors.forEach(e -> System.out.println("  ✗ " + e));
                         }
                     } catch (Exception e) {
                         System.out.println("  ✗ ERROR: " + e.getMessage());
                         allErrors.add(new ValidationError(htmlFile, -1, "Parse failed: " + e.getMessage()));
                     }
                 });
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SUMMARY: " + allErrors.size() + " errors found in total");
        
        if (!allErrors.isEmpty()) {
            System.out.println("\nAll errors:");
            allErrors.forEach(System.out::println);
            System.exit(1);
        }
    }

    private static List<ValidationError> validateTemplate(String html, Path file) {
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            var tokenizer = new HtmlTokenizer();
            var tokens = tokenizer.tokenize(html);
            var parser = new PartParser(tokens);
            var parts = parser.parse();
            
            // Stack-based validation
            Deque<TagInfo> stack = new ArrayDeque<>();
            
            for (int i = 0; i < parts.size(); i++) {
                Part p = parts.get(i);
                if (!(p instanceof Tag tag)) continue;
                
                if (tag.getTagType() == TagType.OPENING) {
                    stack.push(new TagInfo(tag.getLocalName(), i));
                } else if (tag.getTagType() == TagType.CLOSING) {
                    if (stack.isEmpty()) {
                        errors.add(new ValidationError(file, i, 
                            "Closing tag </" + tag.getLocalName() + "> without matching opening tag"));
                    } else {
                        TagInfo opening = stack.peek();
                        if (!opening.name.equals(tag.getLocalName())) {
                            errors.add(new ValidationError(file, i,
                                "Tag mismatch: expected </" + opening.name + "> at part " + opening.index + 
                                ", found </" + tag.getLocalName() + ">"));
                            // Try to find matching tag in stack
                            boolean found = false;
                            for (TagInfo t : stack) {
                                if (t.name.equals(tag.getLocalName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                // Pop until we find it
                                while (!stack.isEmpty() && !stack.peek().name.equals(tag.getLocalName())) {
                                    stack.pop();
                                }
                                if (!stack.isEmpty()) stack.pop();
                            }
                        } else {
                            stack.pop();
                        }
                    }
                }
            }
            
            // Check for unclosed tags
            while (!stack.isEmpty()) {
                TagInfo unclosed = stack.pop();
                errors.add(new ValidationError(file, unclosed.index,
                    "Unclosed tag <" + unclosed.name + "> opened at part " + unclosed.index));
            }
            
        } catch (Exception e) {
            errors.add(new ValidationError(file, -1, "Validation failed: " + e.getMessage()));
        }
        
        return errors;
    }

    record TagInfo(String name, int index) {}
    
    record ValidationError(Path file, int partIndex, String message) {
        @Override
        public String toString() {
            return file.getFileName() + " [part " + partIndex + "]: " + message;
        }
    }
}
