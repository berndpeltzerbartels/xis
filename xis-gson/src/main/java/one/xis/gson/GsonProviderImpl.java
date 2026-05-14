package one.xis.gson;

import com.google.gson.Gson;
import lombok.Data;

@Data
class GsonProviderImpl implements GsonProvider {
    private final Gson gson;
}
