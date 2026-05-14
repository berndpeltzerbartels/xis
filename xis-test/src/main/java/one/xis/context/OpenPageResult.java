package one.xis.context;

/**
 * @deprecated Use {@link TestClient}. This alias remains for compatibility.
 */
@Deprecated
public class OpenPageResult extends TestClient {

    public OpenPageResult(AppContext appContext, IntegrationTestEnvironment testEnvironment) {
        super(appContext, testEnvironment);
    }
}
