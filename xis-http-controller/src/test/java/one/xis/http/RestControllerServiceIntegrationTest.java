package one.xis.http;

import one.xis.context.EventEmitter;
import one.xis.utils.lang.FieldUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ControllerService Integration Test")
class RestControllerServiceIntegrationTest {

    private RestControllerServiceImpl restControllerService;

    @Mock
    private ResponseWriter responseWriter;
    @Mock
    private HttpRequest request;
    @Mock
    private HttpResponse response;

    @Mock
    private EventEmitter eventEmitter;

    @Captor
    private ArgumentCaptor<Object> responseValueCaptor;
    @Captor
    private ArgumentCaptor<Method> responseMethodCaptor;

    @Nested
    @DisplayName("Method Invocation Tests")
    class MethodInvocationTests {

        @Spy
        private TestController testController = new TestController();

        @BeforeEach
        void setUp() {
            restControllerService = new RestControllerServiceImpl();
            // Injiziere Mocks und Spies manuell, da wir kein DI-Framework im Test haben
            FieldUtil.setFieldValue(restControllerService, "responseWriter", responseWriter);
            FieldUtil.setFieldValue(restControllerService, "controllers", List.of(testController));
            FieldUtil.setFieldValue(restControllerService, "eventEmitter", eventEmitter);
            restControllerService.initMethods(); // Simuliert @Init
        }

        @Test
        @DisplayName("should invoke correct method for static path")
        void shouldInvokeCorrectMethodForStaticPath() {
            when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
            when(request.getPath()).thenReturn("/api/users");

            restControllerService.doInvocation(request, response);

            verify(testController).getAllUsers();
            verify(responseWriter).write(eq("all-users"), any(Method.class), eq(request), eq(response));
        }

        @Test
        @DisplayName("should invoke correct method with path variable")
        void shouldInvokeCorrectMethodWithPathVariable() {
            when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
            when(request.getPath()).thenReturn("/api/users/123");

            restControllerService.doInvocation(request, response);

            verify(testController).getUserById("123");
            verify(responseWriter).write(eq("user-123"), any(Method.class), eq(request), eq(response));
        }

        @Test
        @DisplayName("should invoke method with request body")
        void shouldInvokeMethodWithRequestBody() {
            when(request.getHttpMethod()).thenReturn(HttpMethod.POST);
            when(request.getPath()).thenReturn("/api/users");
            when(request.getBodyAsString()).thenReturn("test-user");

            restControllerService.doInvocation(request, response);

            verify(testController).createUser("test-user");
            verify(responseWriter).write(eq("created-test-user"), any(Method.class), eq(request), eq(response));
        }

        @Test
        @DisplayName("should invoke method with header and cookie parameters")
        void shouldInvokeMethodWithHeaderAndCookie() {
            when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
            when(request.getPath()).thenReturn("/api/info");
            when(request.getHeader("X-Test-Header")).thenReturn("header-val");
            when(request.getHeader("Cookie")).thenReturn("test_cookie=cookie-val");

            restControllerService.doInvocation(request, response);

            verify(testController).getInfo("header-val", "cookie-val");
            verify(responseWriter).write(eq("header:header-val;cookie:cookie-val"), any(Method.class), eq(request), eq(response));
        }

        @Test
        @DisplayName("should set 404 for non-existing path")
        void shouldSet404ForNonExistingPath() {
            when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
            when(request.getPath()).thenReturn("/non-existing");

            restControllerService.doInvocation(request, response);

            verify(response).setStatusCode(404);
        }
    }

// ...

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        private static final int CONTROLLER_TYPES = 10;
        private static final int INSTANCES_PER_TYPE = 10;
        private static final int TOTAL_CONTROLLERS = CONTROLLER_TYPES * INSTANCES_PER_TYPE;
        private static final int INVOCATION_COUNT = 1000;

        // Controller-Definitionen mit jeweils zwei Methoden
        @Controller
        class PerfController0 {
            @Get("/perf/0/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "0";
            }

            @Get("/perf/0/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "0-3";
            }
        }

        @Controller
        class PerfController1 {
            @Get("/perf/1/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "1";
            }

            @Get("/perf/1/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "1-3";
            }
        }

        @Controller
        class PerfController2 {
            @Get("/perf/2/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "2";
            }

            @Get("/perf/2/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "2-3";
            }
        }

        @Controller
        class PerfController3 {
            @Get("/perf/3/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "3";
            }

            @Get("/perf/3/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "3-3";
            }
        }

        @Controller
        class PerfController4 {
            @Get("/perf/4/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "4";
            }

            @Get("/perf/4/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "4-3";
            }
        }

        @Controller
        class PerfController5 {
            @Get("/perf/5/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "5";
            }

            @Get("/perf/5/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "5-3";
            }
        }

        @Controller
        class PerfController6 {
            @Get("/perf/6/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "6";
            }

            @Get("/perf/6/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "6-3";
            }
        }

        @Controller
        class PerfController7 {
            @Get("/perf/7/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "7";
            }

            @Get("/perf/7/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "7-3";
            }
        }

        @Controller
        class PerfController8 {
            @Get("/perf/8/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "8";
            }

            @Get("/perf/8/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "8-3";
            }
        }

        @Controller
        class PerfController9 {
            @Get("/perf/9/{id}")
            public String oneVar(@PathVariable("id") String id) {
                return "9";
            }

            @Get("/perf/9/{a}/{b}/{c}")
            public String threeVars(@PathVariable("a") String a, @PathVariable("b") String b, @PathVariable("c") String c) {
                return "9-3";
            }
        }

        @BeforeEach
        void setUp() throws Exception {
            List<Object> controllers = new ArrayList<>();
            for (int i = 0; i < INSTANCES_PER_TYPE; i++) {
                controllers.add(new PerfController0());
                controllers.add(new PerfController1());
                controllers.add(new PerfController2());
                controllers.add(new PerfController3());
                controllers.add(new PerfController4());
                controllers.add(new PerfController5());
                controllers.add(new PerfController6());
                controllers.add(new PerfController7());
                controllers.add(new PerfController8());
                controllers.add(new PerfController9());
            }

            restControllerService = new RestControllerServiceImpl();
            FieldUtil.setFieldValue(restControllerService, "responseWriter", responseWriter);
            FieldUtil.setFieldValue(restControllerService, "controllers", controllers);
            FieldUtil.setFieldValue(restControllerService, "eventEmitter", eventEmitter);

            long startInit = System.nanoTime();
            restControllerService.initMethods();
            long endInit = System.nanoTime();
            System.out.printf("Initialization with %d controllers (%d types) took: %.2f ms%n", TOTAL_CONTROLLERS, CONTROLLER_TYPES, (endInit - startInit) / 1_000_000.0);

            when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
        }

        @Test
        @DisplayName("should handle requests with one path variable quickly")
        void performanceTestWithOnePathVariable() {
            when(request.getPath()).thenReturn("/perf/7/some-id");

            long startFound = System.nanoTime();
            for (int i = 0; i < INVOCATION_COUNT; i++) {
                restControllerService.doInvocation(request, response);
            }
            long endFound = System.nanoTime();

            System.out.printf("Routing %d paths with one variable took: %.2f ms%n", INVOCATION_COUNT, (endFound - startFound) / 1_000_000.0);
            verify(responseWriter, atLeast(INVOCATION_COUNT)).write(eq("7"), any(), eq(request), eq(response));
        }

        @Test
        @DisplayName("should handle requests with three path variables quickly")
        void performanceTestWithThreePathVariables() {
            when(request.getPath()).thenReturn("/perf/8/val1/val2/val3");

            long startFound = System.nanoTime();
            for (int i = 0; i < INVOCATION_COUNT; i++) {
                restControllerService.doInvocation(request, response);
            }
            long endFound = System.nanoTime();

            System.out.printf("Routing %d paths with three variables took: %.2f ms%n", INVOCATION_COUNT, (endFound - startFound) / 1_000_000.0);
            verify(responseWriter, atLeast(INVOCATION_COUNT)).write(eq("8-3"), any(), eq(request), eq(response));
        }

        @Test
        @DisplayName("should handle non-existing paths quickly (404)")
        void performanceTestForNotFound() {
            when(request.getPath()).thenReturn("/non-existing-path-for-performance-test");

            long startNotFound = System.nanoTime();
            for (int i = 0; i < INVOCATION_COUNT; i++) {
                restControllerService.doInvocation(request, response);
            }
            long endNotFound = System.nanoTime();

            System.out.printf("Routing %d non-existing paths (404) took: %.2f ms%n", INVOCATION_COUNT, (endNotFound - startNotFound) / 1_000_000.0);
            verify(response, atLeast(INVOCATION_COUNT)).setStatusCode(404);
            verify(responseWriter, never()).write(any(), any(), any(), any());
        }
    }
}