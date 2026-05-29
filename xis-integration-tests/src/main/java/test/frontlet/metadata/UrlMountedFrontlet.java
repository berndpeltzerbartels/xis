package test.frontlet.metadata;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.PathVariable;

@Frontlet(url = "/frontlet-url/{segment}.html", containerId = "url-mounted")
class UrlMountedFrontlet {

    @ModelData
    String segment(@PathVariable("segment") String segment) {
        return segment;
    }
}
