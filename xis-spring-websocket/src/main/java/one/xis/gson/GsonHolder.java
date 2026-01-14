package one.xis.gson;

import com.google.gson.Gson;
import lombok.Getter;

public class GsonHolder {

    @Getter
    private static final Gson gson = new GsonFactory().gson();
}
