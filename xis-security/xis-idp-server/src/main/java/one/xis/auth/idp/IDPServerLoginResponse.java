package one.xis.auth.idp;

import one.xis.PageUrlResponse;


public class IDPServerLoginResponse extends PageUrlResponse {


    public IDPServerLoginResponse(String loginSuccessRedirectUrl, String state, String code) {
        super(loginSuccessRedirectUrl + "?code=" + code + "&state=" + state);
    }
    
}
