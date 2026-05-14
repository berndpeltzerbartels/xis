package one.xis.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FilterChain Tests")
class FilterChainTest {

    @Mock
    private HttpRequest request;

    @Mock
    private HttpResponse response;

    @Mock
    private BiConsumer<HttpRequest, HttpResponse> controllerInvocation;

    @Nested
    @DisplayName("Filter Execution Order")
    class FilterExecutionOrderTests {

        @Test
        @DisplayName("should execute filters in order and then controller")
        void shouldExecuteFiltersInOrderAndThenController() {
            List<String> executionOrder = new ArrayList<>();

            HttpFilter filter1 = (req, resp, chain) -> {
                executionOrder.add("filter1-before");
                chain.doFilter(req, resp);
                executionOrder.add("filter1-after");
            };

            HttpFilter filter2 = (req, resp, chain) -> {
                executionOrder.add("filter2-before");
                chain.doFilter(req, resp);
                executionOrder.add("filter2-after");
            };

            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) ->
                    executionOrder.add("controller");

            FilterChain chain = new FilterChainImpl(List.of(filter1, filter2), controller);
            chain.doFilter(request, response);

            assertEquals(List.of(
                    "filter1-before",
                    "filter2-before",
                    "controller",
                    "filter2-after",
                    "filter1-after"
            ), executionOrder);
        }

        @Test
        @DisplayName("should execute controller directly when no filters present")
        void shouldExecuteControllerWhenNoFilters() {
            AtomicBoolean controllerExecuted = new AtomicBoolean(false);
            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) ->
                    controllerExecuted.set(true);

            FilterChain chain = new FilterChainImpl(List.of(), controller);
            chain.doFilter(request, response);

            assertTrue(controllerExecuted.get());
        }

        @Test
        @DisplayName("should pass request and response through chain")
        void shouldPassRequestAndResponseThroughChain() {
            HttpFilter filter = (request, response, chain) -> chain.doFilter(request, response);

            FilterChain chain = new FilterChainImpl(List.of(filter), controllerInvocation);
            chain.doFilter(request, response);

            verify(controllerInvocation).accept(request, response);
        }
    }

    @Nested
    @DisplayName("Filter Chain Interruption")
    class FilterChainInterruptionTests {

        @Test
        @DisplayName("should stop chain when filter does not call chain.doFilter")
        void shouldStopChainWhenFilterDoesNotCallNext() {
            AtomicBoolean controllerExecuted = new AtomicBoolean(false);
            AtomicBoolean filter2Executed = new AtomicBoolean(false);

            HttpFilter filter1 = (req, resp, chain) -> {
                // Intentionally not calling chain.doFilter()
                // This simulates a filter that blocks the request
            };

            HttpFilter filter2 = (req, resp, chain) -> {
                filter2Executed.set(true);
                chain.doFilter(req, resp);
            };

            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) ->
                    controllerExecuted.set(true);

            FilterChain chain = new FilterChainImpl(List.of(filter1, filter2), controller);
            chain.doFilter(request, response);

            assertFalse(filter2Executed.get(), "Filter 2 should not be executed");
            assertFalse(controllerExecuted.get(), "Controller should not be executed");
        }

        @Test
        @DisplayName("should allow filter to set response and skip controller")
        void shouldAllowFilterToSetResponseAndSkipController() {

            HttpFilter authFilter = (req, resp, chain) -> {
                // Simulate authentication failure
                resp.setStatusCode(403);
                resp.setBody("Forbidden");
                // Don't call chain.doFilter() - block the request
            };

            AtomicBoolean controllerExecuted = new AtomicBoolean(false);
            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) ->
                    controllerExecuted.set(true);

            FilterChain chain = new FilterChainImpl(List.of(authFilter), controller);
            chain.doFilter(request, response);

            verify(response).setStatusCode(403);
            verify(response).setBody("Forbidden");
            assertFalse(controllerExecuted.get(), "Controller should not execute after auth failure");
        }
    }

    @Nested
    @DisplayName("Filter Priority")
    class FilterPriorityTests {

        @Test
        @DisplayName("should execute filters according to priority")
        void shouldExecuteFiltersAccordingToPriority() {
            List<String> executionOrder = new ArrayList<>();

            HttpFilter lowPriorityFilter = new HttpFilter() {
                @Override
                public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
                    executionOrder.add("low-priority");
                    chain.doFilter(request, response);
                }

                @Override
                public int getPriority() {
                    return 200; // Higher value = lower priority
                }
            };

            HttpFilter highPriorityFilter = new HttpFilter() {
                @Override
                public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
                    executionOrder.add("high-priority");
                    chain.doFilter(request, response);
                }

                @Override
                public int getPriority() {
                    return 50; // Lower value = higher priority
                }
            };

            HttpFilter mediumPriorityFilter = new HttpFilter() {
                @Override
                public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
                    executionOrder.add("medium-priority");
                    chain.doFilter(request, response);
                }

                @Override
                public int getPriority() {
                    return 100; // Default priority
                }
            };

            // Filters should be sorted by priority before creating the chain
            List<HttpFilter> filters = new ArrayList<>(List.of(lowPriorityFilter, highPriorityFilter, mediumPriorityFilter));
            filters.sort(HttpFilter::compareTo);

            FilterChain chain = new FilterChainImpl(filters, (req, resp) -> {
            });
            chain.doFilter(request, response);

            assertEquals(List.of("high-priority", "medium-priority", "low-priority"), executionOrder,
                    "Filters should execute in priority order (high to low)");
        }
    }

    @Nested
    @DisplayName("Request/Response Modification")
    class RequestResponseModificationTests {

        @Test
        @DisplayName("should allow filters to modify request before controller")
        void shouldAllowFiltersToModifyRequest() {
            AtomicInteger headerModificationCount = new AtomicInteger(0);

            HttpFilter filter1 = (req, resp, chain) -> {
                req.addHeader("X-Filter-1", "processed");
                headerModificationCount.incrementAndGet();
                chain.doFilter(req, resp);
            };

            HttpFilter filter2 = (req, resp, chain) -> {
                req.addHeader("X-Filter-2", "processed");
                headerModificationCount.incrementAndGet();
                chain.doFilter(req, resp);
            };

            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) -> {
                // Controller sees modified request
            };

            FilterChain chain = new FilterChainImpl(List.of(filter1, filter2), controller);
            chain.doFilter(request, response);

            verify(request).addHeader("X-Filter-1", "processed");
            verify(request).addHeader("X-Filter-2", "processed");
            assertEquals(2, headerModificationCount.get());
            verify(controllerInvocation, never()).accept(any(), any()); // We used our own controller
        }

        @Test
        @DisplayName("should allow filters to modify response after controller")
        void shouldAllowFiltersToModifyResponseAfterController() {
            List<String> modifications = new ArrayList<>();

            HttpFilter filter = (req, resp, chain) -> {
                modifications.add("before-controller");
                chain.doFilter(req, resp);
                modifications.add("after-controller");
                resp.addHeader("X-Processed", "true");
            };

            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) ->
                    modifications.add("controller");

            FilterChain chain = new FilterChainImpl(List.of(filter), controller);
            chain.doFilter(request, response);

            assertEquals(List.of("before-controller", "controller", "after-controller"), modifications);
            verify(response).addHeader("X-Processed", "true");
        }
    }

    @Nested
    @DisplayName("Multiple Filter Chain Execution")
    class MultipleFilterChainTests {

        @Test
        @DisplayName("should handle multiple filters with complex interactions")
        void shouldHandleMultipleFiltersWithComplexInteractions() {
            AtomicInteger executionCounter = new AtomicInteger(0);

            HttpFilter loggingFilter = (req, resp, chain) -> {
                int order = executionCounter.incrementAndGet();
                chain.doFilter(req, resp);
            };

            HttpFilter corsFilter = (req, resp, chain) -> {
                executionCounter.incrementAndGet();
                resp.addHeader("Access-Control-Allow-Origin", "*");
                chain.doFilter(req, resp);
            };

            HttpFilter cachingFilter = (req, resp, chain) -> {
                executionCounter.incrementAndGet();
                String cacheKey = req.getPath();
                chain.doFilter(req, resp);
                resp.addHeader("X-Cache-Key", cacheKey);
            };

            AtomicBoolean controllerExecuted = new AtomicBoolean(false);
            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) -> {
                executionCounter.incrementAndGet();
                controllerExecuted.set(true);
                executionCounter.incrementAndGet();
            };

            when(request.getPath()).thenReturn("/api/test");

            FilterChain chain = new FilterChainImpl(
                    List.of(loggingFilter, corsFilter, cachingFilter),
                    controller
            );
            chain.doFilter(request, response);

            assertTrue(controllerExecuted.get());
            verify(response).addHeader("Access-Control-Allow-Origin", "*");
            verify(response).addHeader("X-Cache-Key", "/api/test");
            assertTrue(executionCounter.get() >= 4, "All filters and controller should execute");
        }

        @Test
        @DisplayName("should handle empty filter list gracefully")
        void shouldHandleEmptyFilterListGracefully() {
            AtomicBoolean controllerExecuted = new AtomicBoolean(false);
            BiConsumer<HttpRequest, HttpResponse> controller = (req, resp) ->
                    controllerExecuted.set(true);

            FilterChain chain = new FilterChainImpl(new ArrayList<>(), controller);
            chain.doFilter(request, response);

            assertTrue(controllerExecuted.get(), "Controller should execute even with no filters");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should propagate exceptions from filters")
        void shouldPropagateExceptionsFromFilters() {
            HttpFilter failingFilter = (req, resp, chain) -> {
                throw new RuntimeException("Filter failed");
            };

            FilterChain chain = new FilterChainImpl(List.of(failingFilter), controllerInvocation);

            assertThrows(RuntimeException.class, () -> chain.doFilter(request, response));
            verify(controllerInvocation, never()).accept(any(), any());
        }

        @Test
        @DisplayName("should propagate exceptions from controller")
        void shouldPropagateExceptionsFromController() {
            BiConsumer<HttpRequest, HttpResponse> failingController = (req, resp) -> {
                throw new IllegalStateException("Controller failed");
            };

            HttpFilter filter = (req, resp, chain) -> chain.doFilter(req, resp);

            FilterChain chain = new FilterChainImpl(List.of(filter), failingController);

            assertThrows(IllegalStateException.class, () -> chain.doFilter(request, response));
        }

        @Test
        @DisplayName("should allow filter to catch and handle controller exceptions")
        void shouldAllowFilterToCatchControllerExceptions() {
            BiConsumer<HttpRequest, HttpResponse> failingController = (req, resp) -> {
                throw new IllegalArgumentException("Invalid request");
            };

            AtomicBoolean errorHandled = new AtomicBoolean(false);

            HttpFilter errorHandlingFilter = (req, resp, chain) -> {
                try {
                    chain.doFilter(req, resp);
                } catch (IllegalArgumentException e) {
                    errorHandled.set(true);
                    resp.setStatusCode(400);
                    resp.setBody("Bad Request: " + e.getMessage());
                }
            };

            FilterChain chain = new FilterChainImpl(List.of(errorHandlingFilter), failingController);

            assertDoesNotThrow(() -> chain.doFilter(request, response));
            assertTrue(errorHandled.get());
            verify(response).setStatusCode(400);
            verify(response).setBody("Bad Request: Invalid request");
        }
    }

    @Nested
    @DisplayName("HttpFilter Comparable Implementation")
    class HttpFilterComparableTests {

        @Test
        @DisplayName("should compare filters by priority correctly")
        void shouldCompareFiltersByPriority() {
            HttpFilter highPriority = new TestFilter(10);
            HttpFilter lowPriority = new TestFilter(100);

            assertTrue(highPriority.compareTo(lowPriority) < 0,
                    "Lower priority value should come first");
            assertTrue(lowPriority.compareTo(highPriority) > 0,
                    "Higher priority value should come last");
        }

        @Test
        @DisplayName("should treat equal priorities as equal")
        void shouldTreatEqualPrioritiesAsEqual() {
            HttpFilter filter1 = new TestFilter(50);
            HttpFilter filter2 = new TestFilter(50);

            assertEquals(0, filter1.compareTo(filter2));
        }

        @Test
        @DisplayName("should use default priority of 100")
        void shouldUseDefaultPriority() {
            HttpFilter defaultFilter = new HttpFilter() {
                @Override
                public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
                }
            };

            assertEquals(100, defaultFilter.getPriority());
        }

        private static class TestFilter implements HttpFilter {
            private final int priority;

            TestFilter(int priority) {
                this.priority = priority;
            }

            @Override
            public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
            }

            @Override
            public int getPriority() {
                return priority;
            }
        }
    }
}
