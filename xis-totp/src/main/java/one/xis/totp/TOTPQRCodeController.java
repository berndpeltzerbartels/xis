package one.xis.totp;

import one.xis.UserContext;
import one.xis.http.ContentType;
import one.xis.http.Controller;
import one.xis.http.Get;
import one.xis.http.Produces;
import one.xis.http.ResponseEntity;

@Controller("/xis/totp")
class TOTPQRCodeController {

    private final TOTPEnrollmentService enrollmentService;

    TOTPQRCodeController(TOTPEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @Get("/qr.svg")
    @Produces(ContentType.SVG)
    ResponseEntity<String> qrCode() {
        UserContext userContext = UserContext.getInstance();
        if (!userContext.isAuthenticated()) {
            return ResponseEntity.status(401, "");
        }
        String svg = enrollmentService.qrCodeSvg(userContext.getUserId());
        return ResponseEntity.ok(svg)
                .addHeader("Cache-Control", "no-store");
    }
}
