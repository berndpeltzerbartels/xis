package one.xis.totp;

import one.xis.context.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TOTPEnrollmentService {

    private final TOTPProvisioningService provisioningService;
    private final QRCodeSvgRenderer renderer;

    TOTPEnrollmentService(TOTPProvisioningService provisioningService, QRCodeSvgRenderer renderer) {
        this.provisioningService = provisioningService;
        this.renderer = renderer;
    }

    public String qrCodeSvg(String userId) {
        return renderer.render(provisioningService.provisioningUri(userId));
    }

    public String qrCodeDataUrl(String userId) {
        String svg = qrCodeSvg(userId);
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }
}
