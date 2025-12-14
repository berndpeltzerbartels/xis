package one.xis.html;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlParserTest {

    private final HtmlParser parser = new HtmlParser();


    @Nested
    class SimpleElementTest {
        private final String html = "<div class=\"container\">Content</div>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }


    @Nested
    class SimpleNestedElementTest {
        private final String html = "<html><body><div class=\"container\">Content</div></body></html>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }

    @Nested
    class SelfClosingTagClosedTest {
        private final String html = "<img src=\"image.png\" alt=\"An image\"/>";

        private final String expectedHtml = "<img src=\"image.png\" alt=\"An image\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace(expectedHtml);
        }
    }

    @Nested
    class SelfClosingTagNotClosedTest {
        private final String html = "<img src=\"image.png\" alt=\"An image\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<img src=\"image.png\" alt=\"An image\">");
        }
    }


    @Nested
    class HtmlTest {
        private final String html = """
                <html>
                    <head>
                        <title>Test</title>
                    </head>
                    <body>
                        <h1>Hello, World!</h1>
                        <p>This is a simple HTML document.</p>
                    </body>
                </html>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();


            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }

    @Nested
    class HtmlWithXisElementsTest1 {
        private final String html = """
                <a page="home">
                   Linktext
                   <xis:param name="param1" value="value1"/>
                   <xis:param name="param2" value="value2"/>
                </a>
                """;

        private final String expectedHtml = """
                <a page="home">
                   Linktext
                   <xis:param name="param1" value="value1"></xis:param>
                   <xis:param name="param2" value="value2"></xis:param>
                </a>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();


            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.expectedHtml);
        }
    }

    @Nested
    class HtmlWithXisExpressionLanguage {
        private final String html = """
                <div xis:if="${login.attempts > 3}">
                   Too many login attempts.
                </div>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);

            assertThat(document.getDocumentElement().getAttributes()).isEqualTo(Map.of("xis:if", "${login.attempts > 3}"));
        }
    }

    @Nested
    class BooleanAttributeTest {
        private final String html = "<input type=\"checkbox\" checked>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<input type=\"checkbox\" checked=\"true\">");
        }
    }

    @Nested
    class BooleanAttributeInXmlStyleTest {
        private final String html = "<input type=\"checkbox\" checked=\"checked\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<input type=\"checkbox\" checked=\"checked\">");
        }
    }


    @Nested
    class UnknownBooleanAttributeTest {
        private final String html = "<input type=\"checkbox\" xis:abc>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<input type=\"checkbox\" xis:abc=\"true\">");
        }
    }

    @Nested
    class BigTest {
        private final String html = """
                <xis:template xmlns:xis="http://www.w3.org/1999/XSL/Transform">
                    <xis:raw>
                        <p>XIS provides a comprehensive set of framework tags that get normalized into standard HTML during processing.
                            These tags can often be written as both elements and attributes.</p>
                
                        <h6>&lt;xis:foreach&gt;</h6>
                        <p>Iterates over collections to repeat content for each item.</p>
                
                        <p><strong>Element syntax:</strong></p>
                        <pre><code class="language-html">&lt;xis:foreach var="user" array="&#036;&#123;users}"&gt;
                    &lt;div class="user-card"&gt;
                        &lt;h3&gt;&#036;&#123;user.name}&lt;/h3&gt;
                        &lt;p&gt;&#036;&#123;user.email}&lt;/p&gt;
                    &lt;/div&gt;
                &lt;/xis:foreach&gt;</code></pre>
                
                        <p><strong>Attribute syntax:</strong></p>
                        <pre><code class="language-html">&lt;div xis:foreach="user:&#036;&#123;users}"&gt;
                    &lt;div class="user-card"&gt;
                        &lt;h3&gt;&#036;&#123;user.name}&lt;/h3&gt;
                        &lt;p&gt;&#036;&#123;user.email}&lt;/p&gt;
                    &lt;/div&gt;
                &lt;/div&gt;</code></pre>
                
                        <p><strong>Repeat syntax (alternative):</strong></p>
                        <pre><code class="language-html">&lt;div xis:repeat="user:&#036;&#123;users}"&gt;
                    &lt;div class="user-card"&gt;
                        &lt;h3&gt;&#036;&#123;user.name}&lt;/h3&gt;
                        &lt;p&gt;&#036;&#123;user.email}&lt;/p&gt;
                    &lt;/div&gt;
                &lt;/div&gt;</code></pre>
                
                        <h6>&lt;xis:if&gt;</h6>
                        <p>Conditionally renders content based on expressions.</p>
                
                        <p><strong>Element syntax:</strong></p>
                        <pre><code class="language-html">&lt;xis:if condition="&#036;&#123;user.isActive}"&gt;
                    &lt;div class="status-active"&gt;User is active&lt;/div&gt;
                &lt;/xis:if&gt;</code></pre>
                
                        <p><strong>Attribute syntax (xis:if):</strong></p>
                        <pre><code class="language-html">&lt;div xis:if="&#036;&#123;user.isActive}"&gt;
                    &lt;div class="status-active"&gt;User is active&lt;/div&gt;
                &lt;/div&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong> The xis:if attribute surrounds the entire element with an if-tag.</p>
                
                        <h6>&lt;xis:widget-container&gt;</h6>
                        <p>Creates a container for dynamic widget loading.</p>
                
                        <p><strong>Element syntax:</strong></p>
                        <pre><code class="language-html">&lt;xis:widget-container container-id="mainContent"
                                         default-widget="DashboardWidget"&gt;
                &lt;/xis:widget-container&gt;</code></pre>
                
                        <p><strong>Attribute syntax:</strong></p>
                        <pre><code class="language-html">&lt;div xis:widget-container="mainContent"
                     xis:default-widget="DashboardWidget"&gt;
                &lt;/div&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;div&gt;
                    &lt;xis:widget-container container-id="mainContent"\s
                                         default-widget="DashboardWidget"&gt;
                    &lt;/xis:widget-container&gt;
                &lt;/div&gt;</code></pre>
                
                        <h6>&lt;xis:message&gt;</h6>
                        <p>Displays validation messages for form fields.</p>
                
                        <p><strong>Element syntax:</strong></p>
                        <pre><code class="language-html">&lt;input type="text" name="email" /&gt;
                &lt;xis:message message-for="email"&gt;&lt;/xis:message&gt;</code></pre>
                
                        <p><strong>Attribute syntax:</strong></p>
                        <pre><code class="language-html">&lt;div xis:message-for="email"&gt;&lt;/div&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;div&gt;
                    &lt;xis:message message-for="email"&gt;&lt;/xis:message&gt;
                &lt;/div&gt;</code></pre>
                
                        <h6>&lt;xis:parameter&gt;</h6>
                        <p>Passes parameters to actions and widgets.</p>
                
                        <pre><code class="language-html">&lt;button xis:action="deleteUser"&gt;
                    &lt;xis:parameter name="userId" value="&#036;&#123;user.id}"/&gt;
                    &lt;xis:parameter name="confirmRequired" value="true"/&gt;
                    Delete User
                &lt;/button&gt;</code></pre>
                
                        <h5>Form Elements</h5>
                        <p>XIS provides framework alternatives to standard HTML form elements:</p>
                
                        <h6>&lt;xis:form&gt;</h6>
                        <p>XIS form element that gets normalized to standard HTML form.</p>
                
                        <pre><code class="language-html">&lt;xis:form binding="user" action="saveUser"&gt;
                    &lt;xis:input binding="name" type="text"/&gt;
                    &lt;xis:input binding="email" type="email"/&gt;
                    &lt;xis:submit&gt;Save User&lt;/xis:submit&gt;
                &lt;/xis:form&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;form xis:binding="user" xis:action="saveUser"&gt;
                    &lt;input xis:binding="name" type="text"/&gt;
                    &lt;input xis:binding="email" type="email"/&gt;
                    &lt;input type="submit" value="Save User"/&gt;
                &lt;/form&gt;</code></pre>
                
                        <h6>&lt;xis:input&gt;</h6>
                        <p>XIS input element normalized to HTML input.</p>
                
                        <pre><code class="language-html">&lt;xis:input binding="username" type="text" required="true"/&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;input xis:binding="username" type="text" required="true"/&gt;</code></pre>
                
                        <h6>&lt;xis:select&gt;</h6>
                        <p>XIS select element normalized to HTML select.</p>
                
                        <pre><code class="language-html">&lt;xis:select binding="country"&gt;
                    &lt;option value="DE"&gt;Germany&lt;/option&gt;
                    &lt;option value="US"&gt;United States&lt;/option&gt;
                &lt;/xis:select&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;select xis:binding="country"&gt;
                    &lt;option value="DE"&gt;Germany&lt;/option&gt;
                    &lt;option value="US"&gt;United States&lt;/option&gt;
                &lt;/select&gt;</code></pre>
                
                        <h6>&lt;xis:checkbox&gt;</h6>
                        <p>XIS checkbox element normalized to HTML input with type="checkbox".</p>
                
                        <pre><code class="language-html">&lt;xis:checkbox binding="newsletter" value="true"/&gt;
                &lt;label for="newsletter"&gt;Subscribe to newsletter&lt;/label&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;input xis:binding="newsletter" type="checkbox" value="true"/&gt;
                &lt;label for="newsletter"&gt;Subscribe to newsletter&lt;/label&gt;</code></pre>
                
                        <h6>&lt;xis:radio&gt;</h6>
                        <p>XIS radio button element normalized to HTML input with type="radio".</p>
                
                        <pre><code class="language-html">&lt;xis:radio binding="gender" value="male"/&gt;
                &lt;label&gt;Male&lt;/label&gt;
                &lt;xis:radio binding="gender" value="female"/&gt;
                &lt;label&gt;Female&lt;/label&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;input xis:binding="gender" type="radio" value="male"/&gt;
                &lt;label&gt;Male&lt;/label&gt;
                &lt;input xis:binding="gender" type="radio" value="female"/&gt;
                &lt;label&gt;Female&lt;/label&gt;</code></pre>
                
                        <h6>&lt;xis:button&gt;</h6>
                        <p>XIS button element normalized to HTML button.</p>
                
                        <pre><code class="language-html">&lt;xis:button action="submitForm" type="submit"&gt;
                    Submit Form
                &lt;/xis:button&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;button xis:action="submitForm" type="submit"&gt;
                    Submit Form
                &lt;/button&gt;</code></pre>
                
                        <h6>&lt;xis:submit&gt;</h6>
                        <p>XIS submit button element normalized to HTML submit input.</p>
                
                        <pre><code class="language-html">&lt;xis:submit&gt;Save Changes&lt;/xis:submit&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;submit&gt;Save Changes&lt;/submit&gt;</code></pre>
                
                        <h5>Navigation Elements</h5>
                
                        <h6>&lt;xis:a&gt;</h6>
                        <p>XIS link element that gets normalized to standard HTML anchor.</p>
                
                        <pre><code class="language-html">&lt;xis:a page="/user-profile.html" parameters="userId=&#036;&#123;user.id}"&gt;
                    View Profile
                &lt;/xis:a&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;a xis:page="/user-profile.html" xis:parameters="userId=&#036;&#123;user.id}"&gt;
                    View Profile
                &lt;/a&gt;</code></pre>
                
                        <h6>&lt;xis:action&gt;</h6>
                        <p>XIS action link element for triggering controller actions.</p>
                
                        <pre><code class="language-html">&lt;xis:action action="deleteUser" target-container="userList"&gt;
                    &lt;xis:parameter name="userId" value="&#036;&#123;user.id}"/&gt;
                    Delete
                &lt;/xis:action&gt;</code></pre>
                
                        <p><strong>DOM Result:</strong></p>
                        <pre><code class="language-html">&lt;a xis:action="deleteUser" xis:target-container="userList"&gt;
                    Delete
                &lt;/a&gt;</code></pre>
                
                        <h5>Advanced Usage</h5>
                        <p>Tags can be combined and nested for complex scenarios:</p>
                
                        <pre><code class="language-html">&lt;div xis:if="&#036;&#123;users.size&#040;) > 0}"&gt;
                    &lt&lt;div xis:foreach="user:&#036;&#123;users}"&gt;
                        &lt;div class="user-card" xis:if="&#036;&#123;user.isActive}"&gt;
                            &lt;h3&gt;&#036;&#123;user.name}&lt;/h3&gt;
                            &lt&lt;xis:form binding="user"&gt;
                                &lt&lt;xis:input binding="email" type="email"/&gt;
                                &lt&lt;xis:message message-for="email"&gt;&lt;/xis:message&gt;
                                &lt&lt;xis:button action="updateUser"&gt;
                                    &lt&lt;xis:parameter name="userId" value="&#036;&#123;user.id}"/&gt;
                                    Update
                                &lt&lt;/xis:button&gt;
                            &lt&lt;/xis:form&gt;
                        &lt&lt;/div&gt;
                    &lt&lt;/div&gt;
                &lt&lt;/div&gt;</code></pre>
                    </xis:raw>
                </xis:template>
                """;


        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            // assertThat(htmlResult).isEqualToIgnoringWhitespace("<input type=\"checkbox\" checked=\"true\">");
        }
    }

}