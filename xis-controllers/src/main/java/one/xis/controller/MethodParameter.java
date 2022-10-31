package one.xis.controller;


import one.xis.dto.Request;

public interface MethodParameter<T> {

    T valueFromRequest(Request request);

    T getValue();

}
