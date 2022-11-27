package one.xis.ajax;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.security.TokenAnalyzer;

import java.util.List;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
class AjaxRequestContextFactory {

    private final TokenAnalyzer tokenAnalyzer;

    AjaxRequestContext createContext(AjaxRequest request, String clientId, String authorization) {
        var tokenAttributes = tokenAnalyzer.analyze(authorization);
        var clientAttributes = new ClientAttributes(clientId, tokenAttributes.getUserId(), tokenAttributes.getRoles());
        return new AjaxRequestContext(request.getTimestamp(), clientAttributes, toInvocationContexts(request.getMessages(), clientAttributes));
    }

    private List<InvocationContext> toInvocationContexts(List<AjaxRequestMessage> messages, ClientAttributes clientAttributes) {
        return messages.stream().map(message -> toInvocationContext(message, clientAttributes)).collect(Collectors.toList());
    }

    private InvocationContext toInvocationContext(AjaxRequestMessage message, ClientAttributes clientAttributes) {
        var context = new InvocationContext();
        context.setClientAttributes(clientAttributes);
        context.setComponentClass(message.getComponentClass());
        context.setComponentParameters(message.getComponentParameters());
        context.setData(message.getData());
        context.setPhase(message.getPhase());
        context.setType(message.getType());
        return context;
    }

}
