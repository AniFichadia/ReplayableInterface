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


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import static com.aniruddhfichadia.replayableinterface.ReplayableInterfaceProcessor.PACKAGE_REPLAYABLE_INTERFACE;
import static com.aniruddhfichadia.replayableinterface.ReplayableInterfaceProcessor.WEAK_REFERENCE;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-21
 */
public class DelegatorVisitor {
    public static final ClassName DELEGATABLE = ClassName.get(PACKAGE_REPLAYABLE_INTERFACE,
                                                              "Delegator");

    public static final String FIELD_NAME_DELEGATE_REFERENCE = "delegateReference";
    public static final String PARAM_NAME_DELEGATE           = "delegate";

    public static final String METHOD_NAME_IS_DELEGATE_BOUND = "isDelegateBound";
    public static final String METHOD_NAME_BIND_DELEGATE     = "bindDelegate";
    public static final String METHOD_NAME_UN_BIND_DELEGATE  = "unBindDelegate";
    public static final String METHOD_NAME_GET_DELEGATE      = "getDelegate";

    private final TypeSpec.Builder      classBuilder;
    private final ClassName             targetClassName;
    private final ParameterizedTypeName delegateReferenceType;


    public DelegatorVisitor(TypeSpec.Builder classBuilder, ClassName targetClassName) {
        super();

        this.classBuilder = classBuilder;
        this.targetClassName = targetClassName;
        this.delegateReferenceType = ParameterizedTypeName.get(WEAK_REFERENCE, targetClassName);
    }


    public DelegatorVisitor applyClassDefinition() {
        classBuilder.addSuperinterface(ParameterizedTypeName.get(DELEGATABLE, targetClassName));
        return this;
    }

    public DelegatorVisitor applyFields() {
        classBuilder.addField(createFieldDelegateReference());
        return this;
    }

    public DelegatorVisitor applyMethods() {
        classBuilder.addMethod(createMethodBindDelegate())
                    .addMethod(createMethodUnBindDelegate())
                    .addMethod(createMethodIsDelegateBound())
                    .addMethod(createMethodGetDelegate());
        return this;
    }


    private FieldSpec createFieldDelegateReference() {
        return FieldSpec.builder(delegateReferenceType, FIELD_NAME_DELEGATE_REFERENCE)
                        .addModifiers(Modifier.PRIVATE)
                        .initializer(CodeBlock.of("new $T(null)", delegateReferenceType))
                        .build();
    }

    private MethodSpec createMethodBindDelegate() {
        return MethodSpec.methodBuilder(METHOD_NAME_BIND_DELEGATE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .addParameter(ParameterSpec.builder(targetClassName, PARAM_NAME_DELEGATE)
                                                    .build()
                         )
                         .addCode(CodeBlock.builder()
                                           .addStatement("this.$L = new $T($L)",
                                                         FIELD_NAME_DELEGATE_REFERENCE,
                                                         delegateReferenceType,
                                                         PARAM_NAME_DELEGATE)
                                           .build()
                         )
                         .build();
    }

    private MethodSpec createMethodUnBindDelegate() {
        return MethodSpec.methodBuilder(METHOD_NAME_UN_BIND_DELEGATE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .addCode(CodeBlock.builder()
                                           .addStatement("this.$L = new $T(null)",
                                                         FIELD_NAME_DELEGATE_REFERENCE,
                                                         delegateReferenceType)
                                           .build()
                         )
                         .build();
    }

    private MethodSpec createMethodIsDelegateBound() {
        return MethodSpec.methodBuilder(METHOD_NAME_IS_DELEGATE_BOUND)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeName.BOOLEAN)
                         .addCode(CodeBlock.builder()
                                           .addStatement("return this.$L.get() != null",
                                                         FIELD_NAME_DELEGATE_REFERENCE)
                                           .build()
                         )
                         .build();
    }

    private MethodSpec createMethodGetDelegate() {
        return MethodSpec.methodBuilder(METHOD_NAME_GET_DELEGATE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(targetClassName)
                         .addCode(CodeBlock.builder()
                                           .addStatement("return this.$L.get()",
                                                         FIELD_NAME_DELEGATE_REFERENCE)
                                           .build())
                         .build();
    }
}