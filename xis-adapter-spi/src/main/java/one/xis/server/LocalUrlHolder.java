package one.xis.server;

public interface LocalUrlHolder extends UrlHolder {

    void setLocalUrl(String localUrl);

    boolean localUrlIsSet();
    
    boolean isSecure();
}
