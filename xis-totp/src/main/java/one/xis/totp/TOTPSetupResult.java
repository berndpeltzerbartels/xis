package one.xis.totp;

public class TOTPSetupResult {

    private String userId;
    private String qrCodeDataUrl;
    private String error;
    private boolean hasQrCode;
    private boolean hasError;

    static TOTPSetupResult qrCode(String userId, String qrCodeDataUrl) {
        var result = new TOTPSetupResult();
        result.setUserId(userId);
        result.setQrCodeDataUrl(qrCodeDataUrl);
        return result;
    }

    static TOTPSetupResult error(String error) {
        var result = new TOTPSetupResult();
        result.setError(error);
        return result;
    }

    public boolean isHasError() {
        return hasError;
    }

    public boolean isHasQrCode() {
        return hasQrCode;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public void setHasQrCode(boolean hasQrCode) {
        this.hasQrCode = hasQrCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQrCodeDataUrl() {
        return qrCodeDataUrl;
    }

    public void setQrCodeDataUrl(String qrCodeDataUrl) {
        this.qrCodeDataUrl = qrCodeDataUrl;
        this.hasQrCode = qrCodeDataUrl != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.hasError = error != null;
    }
}
