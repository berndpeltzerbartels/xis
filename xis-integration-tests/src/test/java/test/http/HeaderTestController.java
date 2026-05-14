package test.http;

import one.xis.http.Controller;
import one.xis.http.Get;
import one.xis.http.ResponseEntity;

@Controller
public class HeaderTestController {

    @Get("/test-header")
    ResponseEntity<String> header() {
        return ResponseEntity.ok("ok")
                .addHeader("X-Test-Header", "header-value");
    }

    @Get("/test-redirect")
    ResponseEntity<?> redirect() {
        return ResponseEntity.redirect("/target-page.html");
    }
}
