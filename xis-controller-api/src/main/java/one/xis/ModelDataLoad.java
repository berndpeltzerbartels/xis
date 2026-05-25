package one.xis;

/**
 * Controls when a {@link ModelData} or {@link FormData} method is called.
 */
public enum ModelDataLoad {
    /**
     * Call the model method during normal model loading and after actions.
     */
    ALWAYS,

    /**
     * Call the model method only when a page or frontlet is loaded.
     */
    INITIAL,

    /**
     * Call the model method only while rendering the same page or frontlet after an action.
     */
    AFTER_ACTION
}
