/*
 * Copyright (C) 2017 Aniruddh Fichadia
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If you use or enhance the code, please let me know using the provided author information or via email Ani.Fichadia@gmail.com.
 */

package com.aniruddhfichadia.replayableinterface;


import com.aniruddhfichadia.replayableinterface.ReplayableInterface.ReplayType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.aniruddhfichadia.replayableinterface.DelegatorClassBuilder.FIELD_NAME_DELEGATE_REFERENCE;
import static com.aniruddhfichadia.replayableinterface.DelegatorClassBuilder.METHOD_NAME_GET_DELEGATE;
import static com.aniruddhfichadia.replayableinterface.ReplaySourceClassBuilder.METHOD_NAME_ADD_REPLAYABLE_ACTION;
import static com.aniruddhfichadia.replayableinterface.ReplayableActionClassBuilder.FIELD_NAME_PARAMS;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-21
 */
public class ReplayableInterfaceTargetClassBuilder {
    public static final ClassName CLASS_NAME_REPLAY_STRATEGY        = ClassName.get(
            ReplayStrategy.class
    );
    public static final ClassName CLASS_NAME_OBJECT                 = ClassName.get(Object.class);
    public static final ClassName CLASS_NAME_UUID                   = ClassName.get(UUID.class);
    public static final ClassName CLASS_NAME_NULL_POINTER_EXCEPTION = ClassName.get(
            NullPointerException.class
    );

    private static final String VAR_NAME_ACTION_KEY = "_actionKey";
    private static final String VAR_NAME_DELEGATE   = "_delegate";


    private final TypeSpec.Builder classBuilder;
    private final TypeElement      targetClassElement;
    private final Elements         elementUtils;
    private final Types            typeUtils;
    private final ReplayType       replayType;
    private final ReplayStrategy   defaultReplyStrategy;


    private final List<String> warnings;
    private final List<String> errors;


    public ReplayableInterfaceTargetClassBuilder(Builder classBuilder, TypeElement targetClassElement,
                                                 Elements elementUtils, Types typeUtils,
                                                 ReplayType replayType, ReplayStrategy defaultReplyStrategy) {
        super();

        this.classBuilder = classBuilder;
        this.targetClassElement = targetClassElement;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.replayType = replayType;
        this.defaultReplyStrategy = defaultReplyStrategy;
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public ReplayableInterfaceTargetClassBuilder applyClassDefinition() {
        TypeMirror targetClassType = targetClassElement.asType();
        classBuilder.addSuperinterface(TypeName.get(targetClassType));

        List<? extends TypeParameterElement> typeParameters = targetClassElement.getTypeParameters();
        for (TypeParameterElement typeParameter : typeParameters) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeParameter));
        }

        return this;
    }


    public ReplayableInterfaceTargetClassBuilder applyMethods() {
        List<ExecutableElement> methods = getMethodsFromInterface(targetClassElement);

        for (ExecutableElement method : methods) {
            classBuilder.addMethod(createImplementedMethod(method));
        }

        return this;
    }

    private List<ExecutableElement> getMethodsFromInterface(TypeElement element) {
        List<? extends Element> objectMembers = elementUtils.getAllMembers(
                elementUtils.getTypeElement(CLASS_NAME_OBJECT.toString())
        );

        List<? extends Element> allMembers = new ArrayList<>(
                elementUtils.getAllMembers(element)
        );

        allMembers.removeAll(objectMembers);

        return ElementFilter.methodsIn(allMembers);
    }


    private MethodSpec createImplementedMethod(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        List<? extends VariableElement> methodParameters = method.getParameters();
        TypeMirror methodReturnType = method.getReturnType();
        boolean methodReturnsNonVoidValue = methodReturnType.getKind() != TypeKind.VOID;

        ReplayableMethod methodAnnotation = method.getAnnotation(ReplayableMethod.class);
        ReplayStrategy replayStrategy = resolveStrategy(methodAnnotation);
        String group = methodAnnotation == null ? null : methodAnnotation.group();

        if (methodReturnsNonVoidValue) {
            // TODO probably should not get to this point at all
            warnings.add(String.format(
                    "%s returns a value, this will throw a NullPointerException when the delegate is not bound",
                    methodName));
        }

        MethodSpec.Builder methodBuilder =
                MethodSpec.overriding(method, (DeclaredType) targetClassElement.asType(), typeUtils)
                          .addJavadoc("Built using {@link $T#$L}\n", CLASS_NAME_REPLAY_STRATEGY,
                                      replayStrategy);

        StringBuilder allParamNamesBuilder = new StringBuilder();
        StringBuilder allParamTypesBuilder = new StringBuilder();
        CodeBlock.Builder replayOnTargetBuilder =
                CodeBlock.builder()
                         .add("$L.$L(", ReplayableActionClassBuilder.PARAM_NAME_TARGET, methodName);

        for (int i = 0; i < methodParameters.size(); i++) {
            VariableElement parameter = methodParameters.get(i);

            TypeName paramType = TypeName.get(parameter.asType());
            Name paramName = parameter.getSimpleName();

            allParamNamesBuilder.append(paramName);
            allParamTypesBuilder.append(paramType);
            replayOnTargetBuilder.add("($T) $L[$L]", paramType, FIELD_NAME_PARAMS, i);

            if (i < methodParameters.size() - 1) {
                final String separator = ", ";

                allParamNamesBuilder.append(separator);
                allParamTypesBuilder.append(separator);
                replayOnTargetBuilder.add(separator);
            }
        }
        replayOnTargetBuilder.add(");\n");

        String allParamNames = allParamNamesBuilder.toString();
        String allParamTypes = allParamTypesBuilder.toString();

        CodeBlock.Builder methodCode = CodeBlock.builder();
        methodCode.addStatement("$T $L = $L()", targetClassElement, VAR_NAME_DELEGATE,
                                METHOD_NAME_GET_DELEGATE);
        methodCode.beginControlFlow("if ($L != null)", VAR_NAME_DELEGATE);
        if (methodReturnsNonVoidValue) {
            methodBuilder.addException(CLASS_NAME_NULL_POINTER_EXCEPTION);
            methodBuilder.addJavadoc("@throws $T if {@link $L} is null\n",
                                     CLASS_NAME_NULL_POINTER_EXCEPTION,
                                     FIELD_NAME_DELEGATE_REFERENCE);

            methodCode.addStatement("return $L.$L($L)", VAR_NAME_DELEGATE, methodName,
                                    allParamNames)
                      .nextControlFlow("else")
                      .addStatement("throw new $T($S)", CLASS_NAME_NULL_POINTER_EXCEPTION,
                                    FIELD_NAME_DELEGATE_REFERENCE + " contains a null reference " +
                                            "because it is not bound. This method cannot be " +
                                            "invoked since it returns a non-void value and is " +
                                            "auto-generated")
                      .endControlFlow();
        } else {
            methodCode.addStatement("$L.$L($L)", VAR_NAME_DELEGATE, methodName, allParamNames);

            if (ReplayType.DELEGATE_AND_REPLAY == replayType) {
                methodCode.endControlFlow();
            }

            if (!ReplayStrategy.NONE.equals(replayStrategy)) {
                if (ReplayType.DELEGATE_OR_REPLAY == replayType) {
                    methodCode.nextControlFlow("else");
                }

                methodCode.add(createActionKey(methodName, methodParameters, allParamTypes, group,
                                               replayStrategy))
                          .add("");
                methodCode.addStatement("$L($L, $L)", METHOD_NAME_ADD_REPLAYABLE_ACTION,
                                        VAR_NAME_ACTION_KEY,
                                        createAnonymousReplayableAction(allParamNames,
                                                                        replayOnTargetBuilder.build()));
            }

            if (ReplayType.DELEGATE_OR_REPLAY == replayType) {
                methodCode.endControlFlow();
            }
        }

        methodBuilder.addCode(methodCode.build());

        return methodBuilder.build();
    }

    private ReplayStrategy resolveStrategy(ReplayableMethod methodAnnotation) {
        if (methodAnnotation != null) {
            ReplayStrategy methodReplayStrategy = methodAnnotation.value();

            return methodReplayStrategy != ReplayStrategy.DEFAULT
                   ? methodReplayStrategy
                   : ReplayStrategy.ENQUEUE_LAST_ONLY;
        } else {
            return defaultReplyStrategy;
        }
    }

    private CodeBlock createActionKey(String methodName, List<? extends VariableElement> parameters,
                                      String allParamTypes, String group, ReplayStrategy replayStrategy) {
        CodeBlock.Builder actionKeyCode = CodeBlock.builder()
                                                   .add("String $L = ", VAR_NAME_ACTION_KEY);
        switch (replayStrategy) {
            case ENQUEUE:
                actionKeyCode.addStatement("$T.randomUUID().toString()", CLASS_NAME_UUID);
                break;
            case ENQUEUE_LAST_ONLY:
                actionKeyCode.addStatement("\"$L($L)\"", methodName, allParamTypes);
                break;
            case ENQUEUE_LAST_IN_GROUP:
                actionKeyCode.addStatement("\"group: $L\"", group);
                break;
            case ENQUEUE_PARAM_UNIQUE:
                // Generated a method signature with unique parameter values as part of the
                // signature to avoid hashCode or hash collisions. Also this is more readable
                actionKeyCode.add("\"$L(", methodName);

                for (int i = 0; i < parameters.size(); i++) {
                    VariableElement parameter = parameters.get(i);

                    TypeName paramType = TypeName.get(parameter.asType());
                    Name paramName = parameter.getSimpleName();

                    actionKeyCode.add("$L: \" + ", paramType.toString());

                    if (paramType.isPrimitive()) {
                        actionKeyCode.add("$L", paramName);
                    } else {
                        actionKeyCode.add("($L == null ? \"null\" : $L.hashCode())", paramName,
                                          paramName);
                    }

                    if (i < parameters.size() - 1) {
                        actionKeyCode.add(" + \", ");
                    }
                }
                actionKeyCode.add(" + \")\"");
                actionKeyCode.add(";\n");
                break;
            default:
                throw new IllegalArgumentException("Unsupported ReplayStrategy " + replayStrategy);
        }

        return actionKeyCode.build();
    }


    private TypeSpec createAnonymousReplayableAction(String allParamNames, CodeBlock code) {
        return new ReplayableActionClassBuilder(targetClassElement)
                .constructorArgumentNames(allParamNames)
                .replayOnTargetBody(code)
                .build();
    }


    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getErrors() {
        return errors;
    }
}