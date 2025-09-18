package one.xis.js;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.resource.StringResource;

import java.util.LinkedHashMap;

@XISComponent
@RequiredArgsConstructor
class JavascriptProviderImpl implements JavascriptProvider {

    private final JavascriptExtensionLoader extensionLoader;
    private final JavascriptCompressor javascriptCompressor;
    private final Resources resources;

    @Getter
    private Resource compressedJavascript;

    @Getter
    private Resource sourceMap;

    @XISInit
    public void init() {
        var sources = new LinkedHashMap<String, String>();
        sources.put("bundle.min.js", xisJs());
        sources.putAll(extensionLoader.loadExtensions());
        var result = javascriptCompressor.compress(sources);
        this.compressedJavascript = new StringResource(result.compressed());
        this.sourceMap = new StringResource(result.sourceMap());
    }


    private String xisJs() {
        return resources.getByPath("xis.js").getContent();
    }
}
