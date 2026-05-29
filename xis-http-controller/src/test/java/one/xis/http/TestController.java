package one.xis.http;

// Ein Controller für Testzwecke
@Controller("/api")
class TestController {

    @Get("/users")
    public String getAllUsers() {
        return "all-users";
    }

    @Get("/users/{id}")
    public String getUserById(@PathVariable("id") String id) {
        return "user-" + id;
    }

    @Post("/users")
    public String createUser(@RequestBody(BodyType.TEXT) String user) {
        return "created-" + user;
    }

    @Get("/info")
    public String getInfo(@RequestHeader("X-Test-Header") String headerValue, @CookieValue("test_cookie") String cookieValue) {
        return "header:" + headerValue + ";cookie:" + cookieValue;
    }

    @Head("/users")
    public String headUsers() {
        return "head-users";
    }

    @Options("/users")
    public String optionsUsers() {
        return "options-users";
    }

    @Trace("/trace")
    public String trace() {
        return "trace";
    }
}
