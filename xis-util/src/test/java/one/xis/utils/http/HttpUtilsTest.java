package one.xis.utils.http;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpUtilsTest {

    @Test
    void appendQueryParameters_toUrlWithoutQuery() {
        String url = "http://example.com/path";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");
        params.put("param2", 123);

        String expected = "http://example.com/path?param1=value1&param2=123";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_toUrlWithExistingQuery() {
        String url = "http://example.com/path?existing=true";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");

        String expected = "http://example.com/path?existing=true&param1=value1&param2=value2";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_toUrlWithExistingQueryEndingInAmpersand() {
        String url = "http://example.com/path?existing=true&";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");

        String expected = "http://example.com/path?existing=true&param1=value1";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_withEmptyParameters() {
        String url = "http://example.com/path";
        Map<String, Object> params = Collections.emptyMap();

        // The current implementation adds a '?' even if there are no parameters.
        String expected = "http://example.com/path?";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_withEmptyParametersToUrlWithQuery() {
        String url = "http://example.com/path?a=1";
        Map<String, Object> params = Collections.emptyMap();

        // The current implementation adds a '&' even if there are no parameters.
        String expected = "http://example.com/path?a=1&";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_toUrlEndingInQuestionMark() {
        String url = "http://example.com/path?";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");

        // The current implementation adds an extra '&' because the url does not end with it.
        String expected = "http://example.com/path?&param1=value1";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }


    @Nested
    class LocalizeUrlTests {

        @Test
        void localizeUrl_withProtocolAndHost() {
            String url = "http://example.com/path/to/resource";
            String expected = "/path/to/resource";
            String actual = HttpUtils.localizeUrl(url);
            assertEquals(expected, actual);
        }

        @Test
        void localizeUrl_withProtocolRelativeUrl() {
            String url = "//example.com/path/to/resource";
            String expected = "/path/to/resource";
            String actual = HttpUtils.localizeUrl(url);
            assertEquals(expected, actual);
        }

        @Test
        void localizeUrl_withOnlyPath() {
            String url = "/path/to/resource";
            String expected = "/path/to/resource";
            String actual = HttpUtils.localizeUrl(url);
            assertEquals(expected, actual);
        }

        @Test
        void localizeUrl_withJavascriptUrl() {
            String url = "javascript:alert('Hello')";
            String expected = "/";
            String actual = HttpUtils.localizeUrl(url);
            assertEquals(expected, actual);
        }

        @Test
        void localizeUrl_withDataUrl() {
            String url = "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ==";
            String expected = "/";
            String actual = HttpUtils.localizeUrl(url);
            assertEquals(expected, actual);
        }
    }

    @Test
    void parseQueryParameters() {
        String query = "param1=value1&param2=value2&param3=value3";
        Map<String, String> expected = Map.of(
                "param1", "value1",
                "param2", "value2",
                "param3", "value3"
        );

        Map<String, String> actual = HttpUtils.parseQueryParameters(query);

        assertEquals(expected, actual);
    }
}