package one.xis.auth.idp;

import lombok.NonNull;
import one.xis.context.XISComponent;
import one.xis.utils.http.HttpUtils;

import java.net.HttpURLConnection;

@XISComponent
class ExternalIDPConnectionFactory {

    HttpURLConnection createPostConnectionFormUrlEncoded(@NonNull String url, @NonNull String requestBody) {
        return HttpUtils.createPostConnectionFormUrlEncoded(url, requestBody);
    }
}
