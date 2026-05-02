// Reactive State Implementation Plan for XIS
// Branch: reactive-state-II

/**
 * REACTIVE STATE ARCHITECTURE
 * ==========================
 * 
 * Current State:
 * - ClientState exists: app.clientState.getValue(path)
 * - ClientStateVariable evaluates state.* expressions in templates
 * - PageController.handleActionResponse() refreshes entire tree
 * - FrontletContainerHandler has refresh mechanism
 * 
 * New Requirements:
 * 1. Page-level actions update state → full tree refresh
 * 2. Frontlet-level actions update state → page refresh, but invoker stops recursion
 * 3. Form actions behave like frontlets
 * 4. State updates trigger reactive template updates
 */

// === Phase 1: Enhanced State Management ===

class ReactiveStateManager {
    constructor() {
        this.subscribers = new Map(); // path -> [callback]
        this.pendingUpdates = new Set();
        this.updating = false;
    }

    /**
     * Update state and trigger reactive updates
     * @param {Object} stateUpdates - Key-value pairs to update
     * @param {Object} context - {source: 'page'|'frontlet'|'form', invoker: handler}
     */
    updateState(stateUpdates, context) {
        // Update the actual state
        for (let [key, value] of Object.entries(stateUpdates)) {
            app.clientState.setValue(key, value);
        }

        // Trigger reactive refresh
        this.triggerReactiveRefresh(context);
    }

    triggerReactiveRefresh(context) {
        if (this.updating) return; // Prevent recursion
        this.updating = true;

        try {
            switch (context.source) {
                case 'page':
                    // Full tree refresh from page level
                    this.refreshPageTree(context.invoker);
                    break;
                case 'frontlet':
                    // Page refresh, but stop at invoking frontlet
                    this.refreshPageExceptInvoker(context.invoker);
                    break;
                case 'form':
                    // Same as frontlet
                    this.refreshPageExceptInvoker(context.invoker);
                    break;
            }
        } finally {
            this.updating = false;
        }
    }

    refreshPageTree(invoker) {
        // Full page refresh - existing mechanism
        let data = app.pageController.getData();
        data.scope = 'TREE';
        app.pageController.doRefresh(data);
    }

    refreshPageExceptInvoker(invokerFrontlet) {
        // Refresh page but mark invoker as "skip refresh"
        let data = app.pageController.getData();
        data.scope = 'TREE_EXCEPT_INVOKER';
        data.skipInvoker = invokerFrontlet?.frontletInstance?.frontlet?.id;
        app.pageController.doRefresh(data);
    }
}

// === Phase 2: Action Handler Modifications ===

// Extend PageController.handleActionResponse
class ReactivePageController extends PageController {
    
    handleActionResponse(response) {
        // Store reactive state updates before normal processing
        if (response.clientStateData) {
            app.reactiveState.updateState(response.clientStateData, {
                source: 'page',
                invoker: null
            });
        }

        // Continue with existing logic (but skip data refresh if state handled it)
        this.handleActionResponseNoContent(response);
        if (response.status == 204) {
            return;
        }
        
        // Skip normal refresh if reactive state already handled it
        if (!response.clientStateData) {
            var data = response.data;
            data.scope = 'TREE';
            this.doRefresh(data);
        }
    }
}

// === Phase 3: Frontlet Handler Modifications ===

// Extend FrontletContainerHandler to handle reactive state
class ReactiveFrontletContainerHandler extends FrontletContainerHandler {

    handleActionResponse(response) {
        // Handle reactive state first
        if (response.clientStateData) {
            app.reactiveState.updateState(response.clientStateData, {
                source: 'frontlet',
                invoker: this
            });
        }

        // Rest of existing logic
        this.backendService.triggerAdditionalReloadsOnDemand(response);
        
        if (response.nextURL) {
            app.pageController.handleActionResponse(response);
        }
        if (response.nextFrontletId) {
            this.ensureFrontletBound(response.nextFrontletId);
        }
        
        // Skip refresh if reactive state handled it
        if (!response.clientStateData) {
            if (!this.frontletState) {
                this.frontletState = new FrontletState(app.pageController.resolvedURL, {});
            }
            var data = response.data;
            this.refreshContainerId(data);
            this.refreshDescendantHandlers(data);
        }
    }

    refresh(data) {
        // Check if this frontlet should skip refresh (anti-recursion)
        if (data.scope === 'TREE_EXCEPT_INVOKER' && 
            data.skipInvoker === this.currentFrontletId()) {
            return; // Skip refresh for invoking frontlet
        }

        // Continue with normal refresh
        this.refreshContainerId(data);
        this.bindDefaultFrontletInitial(data);
        var frontletParameters = this.frontletState ? this.frontletState.frontletParameters : {};
        this.frontletState = new FrontletState(app.pageController.resolvedURL, frontletParameters);
        if (this.frontletInstance) {
            this.reloadDataAndRefresh(data);
        }
    }
}

// === Phase 4: Server-Side Integration ===

// Java: Extend ControllerMethodResultMapper
public class ReactiveControllerMethodResultMapper extends ControllerMethodResultMapper {
    
    @Override
    void mapReturnValueToResult(ControllerMethodResult result, Method method, Object returnValue, Map<String, Object> requestScope) {
        // Handle existing logic first
        super.mapReturnValueToResult(result, method, returnValue, requestScope);
        
        // Check if this is a reactive state method
        if (method.isAnnotationPresent(ReactiveState.class)) {
            String stateKey = method.getAnnotation(ReactiveState.class).value();
            if (stateKey.isEmpty()) {
                stateKey = getModelDataKey(method);
            }
            result.getClientState().put(stateKey, returnValue);
            result.setReactiveUpdate(true);
        }
    }
}

// New annotation for reactive state methods
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReactiveState {
    String value() default "";
}

/**
 * USAGE EXAMPLES:
 * ===============
 * 
 * // Page-level reactive state
 * @Page("/dashboard.html")
 * public class DashboardPage {
 *     
 *     @Action("updateGlobalCounter")
 *     @ReactiveState("globalCounter")
 *     public Integer updateCounter(@ClientState("globalCounter") Integer current) {
 *         return current + 1;
 *     }
 * }
 * 
 * // Frontlet-level reactive state (prevents recursion)
 * @Frontlet
 * public class CounterFrontlet {
 *     
 *     @Action("increment")
 *     @ReactiveState("frontletCounter")
 *     public Integer increment(@ClientState("frontletCounter") Integer current) {
 *         return current + 1;
 *     }
 * }
 * 
 * // HTML template using reactive state
 * <div>Global Counter: ${state.globalCounter}</div>
 * <div>Frontlet Counter: ${state.frontletCounter}</div>
 * <xis:frontlet-container container-id="counter" default-frontlet="CounterFrontlet"/>
 */

// === Implementation Steps ===
/*
1. Add ReactiveStateManager to main app
2. Modify ClientRequest/ServerResponse to include reactive context
3. Update ControllerMethodResultMapper for @ReactiveState annotation
4. Extend PageController and FrontletContainerHandler with reactive logic
5. Add recursion prevention in refresh() methods
6. Test with simple counter example
7. Add documentation to copilot-instructions.md
*/