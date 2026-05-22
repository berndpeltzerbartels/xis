package one.xis.distributed;

import lombok.RequiredArgsConstructor;
import one.xis.http.Controller;
import one.xis.http.Get;
import one.xis.http.ResponseEntity;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DistributedHostsController {

    private final XisDistributedConfig config;

    @Get("/xis/distributed/hosts")
    public ResponseEntity<List<String>> getHosts() {
        return ResponseEntity.ok(config.getHosts());
    }
}
