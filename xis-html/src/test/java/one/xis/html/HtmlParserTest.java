package one.xis.html;

import one.xis.html.document.Element;
import one.xis.html.document.TextNode;
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

            var input = document.getDocumentElement();
            assertThat(input.getAttributes().get("checked")).isEqualTo("true");
            assertThat(input.getAttributes().get("type")).isEqualTo("checkbox");

        }
    }

    @Nested
    class BooleanAttributeInXmlStyleTest {
        private final String html = "<input type=\"checkbox\" checked=\"checked\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            var input = document.getDocumentElement();
            assertThat(input.getAttributes().get("checked")).isEqualTo("checked");
            assertThat(input.getAttributes().get("type")).isEqualTo("checkbox");
        }
    }

    @Nested
    class CommentTest {
        private final String html = """
                <div>
                    <!-- This is a comment -->
                    <p>Content</p>
                </div>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);

            assertThat(document.getDocumentElement()).isNotNull();

            var div = document.getDocumentElement();
            assertThat(div.getLocalName()).isEqualTo("div");

            var commentNode = div.getFirstChild();
            assertThat(commentNode.getNodeType()).isEqualTo(8); // COMMENT_NODE
            assertThat(commentNode.toHtml().trim()).isEqualTo("<!-- This is a comment -->");
            assertThat(commentNode.getParentNode()).isEqualTo(div);
            assertThat(commentNode.getNextSibling().getNodeType()).isEqualTo(1); // ELEMENT_NODE

            var p = (Element) commentNode.getNextSibling();
            assertThat(p.getLocalName()).isEqualTo("p");
            assertThat(p.getFirstChild().getNodeType()).isEqualTo(3); // TEXT_NODE
            assertThat(p.getFirstChild().toHtml().trim()).isEqualTo("Content");

        }
    }


    @Nested
    class IgnoreScriptTest {
        private final String html = """
                <script>
                // Add custom EL functions
                    elFunctions.addFunction('formatCurrency', (value, currency) => {
                        return new Intl.NumberFormat('en-US', {
                            style: 'currency',
                            currency: currency || 'USD'
                        }).format(value);
                    });
                
                    elFunctions.addFunction('capitalize', (str) => {
                        if (!str || typeof str !== 'string') return str;
                        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
                    });
                
                    elFunctions.addFunction('pluralize', (count, singular, plural) => {
                        return count === 1 ? singular : (plural || singular + 's');
                    });
                
                    elFunctions.addFunction('truncate', (str, maxLength) => {
                        if (!str || typeof str !== 'string') return str;
                        if (str.length <= maxLength) return str;
                        return str.substring(0, maxLength) + '...';
                            });
                </script>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);

            assertThat(document.getDocumentElement()).isNotNull();

            var script = document.getDocumentElement();
            assertThat(script.getLocalName()).isEqualTo("script");
            assertThat(script.getFirstChild().getNodeType()).isEqualTo(3); // TEXT_NODE
        }
    }

    @Nested
    class GlobalMessagesTest {
        private final String html = """
                <xis:template xmlns:xis="https://xis.one/xsd">
                                   <h1>Add Contact</h1>
                
                                   <xis:form binding="contact">
                                       <xis:global-messages/>
                
                                       <h4>Personal Information</h4>
                
                                       <div class="col2">
                                           <div>
                                               <input xis:binding="firstName" type="text" placeholder="First Name"/>
                                               <xis:message message-for="firstName"/>
                                               <label>First Name *</label>
                                           </div>
                
                                           <div>
                                               <input xis:binding="lastName" type="text" placeholder="Last Name"/>
                                               <xis:message message-for="lastName"/>
                                               <label>Last Name *</label>
                                           </div>
                                       </div>
                
                                       <div class="col2">
                                           <div>
                                               <input xis:binding="email" type="email" placeholder="Email"/>
                                               <xis:message message-for="email"/>
                                               <label>Email Address *</label>
                                           </div>
                
                                           <div>
                                               <input xis:binding="phone" type="tel" placeholder="Phone"/>
                                               <xis:message message-for="phone"/>
                                               <label>Phone Number</label>
                                           </div>
                                       </div>
                
                                       <h4>Company Information</h4>
                
                                       <div class="col2">
                                           <div>
                                               <input xis:binding="company" type="text" placeholder="Company"/>
                                               <xis:message message-for="company"/>
                                               <label>Company Name</label>
                                           </div>
                
                                           <div>
                                               <input xis:binding="position" type="text" placeholder="Position"/>
                                               <xis:message message-for="position"/>
                                               <label>Position/Title</label>
                                           </div>
                                       </div>
                
                                       <h4>Classification</h4>
                
                                       <div class="col2">
                                           <div>
                                               <select xis:binding="type">
                                                   <option value="">-- Select Type --</option>
                                                   <option xis:foreach="t:${contactTypes}" value="${t.name}">${t.displayName}</option>
                                               </select>
                                               <xis:message message-for="type"/>
                                               <label>Contact Type *</label>
                                           </div>
                
                                           <div>
                                               <select xis:binding="status">
                                                   <option value="">-- Select Status --</option>
                                                   <option xis:foreach="s:${contactStatuses}" value="${s.name}">${s.displayName}</option>
                                               </select>
                                               <xis:message message-for="status"/>
                                               <label>Status *</label>
                                           </div>
                                           </div>
                
                                           <div class="span0">
                                               <label>Tags (Checkboxes)</label>
                                               <div class="checkbox-group">
                                                   <div xis:foreach="tag:${availableTags}">
                                                       <xis:checkbox binding="tags" value="${tag}" id="tag-${tag}"/>
                                                       <label for="tag-${tag}">${tag}</label>
                                                   </div>
                                               </div>
                                           </div>
                
                
                                   </xis:form>
                
                               </xis:template>
                
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            //assertThat(htmlResult).isEqualToIgnoringWhitespace("<div xis:global-messages=\"true\"></div>");
        }
    }


    @Nested
    class UnknownBooleanAttributeTest {
        private final String html = "<input type=\"checkbox\" xis:abc>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            var input = document.getDocumentElement();
            assertThat(input.getAttributes().get("xis:abc")).isEqualTo("true");
            assertThat(input.getAttributes().get("type")).isEqualTo("checkbox");
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

    // checks a fix  to be valid
    @Nested
    class EvaluationProblemTest {
        private final String html = """
                <div>
                    <a>text</a>
                    <b>sibling</b>
                </div>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            var div = document.getDocumentElement();

            var a = document.getDocumentElement().getElementByTagName("a");
            assertThat(a.getParentNode()).isEqualTo(div);
            var b = document.getDocumentElement().getElementByTagName("b");
            assertThat(b.getParentNode()).isEqualTo(div);

            var textNode = a.getFirstChild();
            assertThat(textNode.getNodeType()).isEqualTo(3); // TEXT_NODE
            assertThat(textNode.toHtml().trim()).isEqualTo("text");

            var textNode2 = b.getFirstChild();
            assertThat(textNode2.getNodeType()).isEqualTo(3); // TEXT_NODE
            assertThat(textNode2.toHtml().trim()).isEqualTo("sibling");

        }

    }


    // checks a fix  to be valid
    @Nested
    class EvaluationProblemTest2 {
        private final String html = "<a>bla<b/></a>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();
            
            var a = document.getDocumentElement().getElementByTagName("a");
            assertThat(a.getFirstChild()).isInstanceOf(TextNode.class);
            var b = document.getDocumentElement().getElementByTagName("b");
            assertThat(a.getFirstChild().getNextSibling()).isEqualTo(b);

        }

    }


}