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
import com.squareup.javapoet.TypeSpec.Builder;

import java.lang.ref.WeakReference;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-21
 */
public class DelegatorWeakReferenceClassBuilder
        extends DelegatorClassBuilder {
    public static final ClassName CLASS_NAME_WEAK_REFERENCE = ClassName.get(WeakReference.class);

    private final ParameterizedTypeName delegateReferenceType;


    public DelegatorWeakReferenceClassBuilder(Builder classBuilder, TypeElement targetClassElement) {
        super(classBuilder, targetClassElement);

        this.delegateReferenceType = ParameterizedTypeName.get(
                CLASS_NAME_WEAK_REFERENCE, targetClassTypeName
        );
    }


    @Override
    protected FieldSpec createFieldDelegateReference() {
        return FieldSpec.builder(delegateReferenceType, FIELD_NAME_DELEGATE_REFERENCE)
                        .addModifiers(Modifier.PRIVATE)
                        .initializer(CodeBlock.of("new $T(null)", delegateReferenceType))
                        .build();
    }


    @Override
    protected MethodSpec createMethodBindDelegate() {
        return MethodSpec.methodBuilder(METHOD_NAME_BIND_DELEGATE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .addParameter(
                                 ParameterSpec.builder(targetClassTypeName, PARAM_NAME_DELEGATE)
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

    @Override
    protected MethodSpec createMethodUnBindDelegate() {
        return MethodSpec.methodBuilder(METHOD_NAME_UN_BIND_DELEGATE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .addCode(CodeBlock.builder()
                                           .addStatement("this.$L = new $T(null)",
                                                         FIELD_NAME_DELEGATE_REFERENCE,
                                                         delegateReferenceType)
                                           .build()
                         )
                         .build();
    }

    @Override
    protected MethodSpec createMethodGetDelegate() {
        return MethodSpec.methodBuilder(METHOD_NAME_GET_DELEGATE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .returns(targetClassTypeName)
                         .addCode(CodeBlock.builder()
                                           .addStatement("return this.$L.get()",
                                                         FIELD_NAME_DELEGATE_REFERENCE)
                                           .build())
                         .build();
    }
}