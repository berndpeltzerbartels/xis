package one.xis.js;

import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class FormHandlerValidationMessageTest {

    @Test
    void submitClearsPreviousValidationMessagesBeforeSendingAction() throws ScriptException {
        var script = Javascript.getScript(CLASSES, FUNCTIONS) + """
                var events = [];
                var formHandler = Object.create(FormHandler.prototype);
                formHandler.binding = 'customer';
                formHandler.frontletParameters = {};
                formHandler.formData = function() {
                    events.push('formData');
                    return {};
                };
                formHandler.frontletId = function() {
                    return null;
                };
                formHandler.targetContainerHandler = function() {
                    return null;
                };
                formHandler.handleActionResponse = function() {
                    events.push('response');
                };
                formHandler.globalMessageHandlers = [{
                    reset() {
                        events.push('global-reset');
                    }
                }];
                formHandler.messageHandlers = {
                    '/customer/name': [{
                        reset() {
                            events.push('field-reset');
                        }
                    }]
                };
                formHandler.client = {
                    formAction() {
                        events.push('formAction');
                        return {
                            then(callback) {
                                callback({ validatorMessages: new ValidatorMessages(), actionProcessing: 'NONE' });
                            }
                        };
                    }
                };
                var app = { pageController: { resolvedURL: {} } };
                formHandler.submit('save');
                events.join(',');
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("global-reset,field-reset,formData,formAction,response");
    }
}
