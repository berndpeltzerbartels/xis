package one.xis.widget;

import lombok.NonNull;
import one.xis.context.XISComponent;
import one.xis.template.ExpressionParser;
import one.xis.template.TemplateDocumentParser;
import one.xis.template.TemplateSynthaxException;
import one.xis.template.WidgetTemplateModel;
import org.w3c.dom.Document;

@XISComponent
class WidgetTemplateDocumentParser extends TemplateDocumentParser<WidgetTemplateModel> {

    WidgetTemplateDocumentParser(ExpressionParser expressionParser) {
        super(expressionParser);
    }

    @Override
    public WidgetTemplateModel parseTemplate(@NonNull Document document, @NonNull String widgetClassName) {
        try {
            return new WidgetTemplateModel(widgetClassName, parseElement(document.getDocumentElement()));
        } catch (TemplateSynthaxException e) {
            throw new TemplateSynthaxException(String.format("Parsing failed for widget '%s': %s", widgetClassName, e.getMessage()));
        }
    }
}
