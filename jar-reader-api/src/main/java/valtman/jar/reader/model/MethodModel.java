package valtman.jar.reader.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class MethodModel {
    private final String methodName;
    private final List<String> exceptions;
    private final String returnType;
    private final List<String> argumentTypes;
    private final List<String> annotations;
    private final String access;
}
