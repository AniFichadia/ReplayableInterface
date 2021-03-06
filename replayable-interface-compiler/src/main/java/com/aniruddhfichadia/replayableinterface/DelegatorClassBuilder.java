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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-21
 */
public abstract class DelegatorClassBuilder {
    public static final ClassName CLASS_NAME_DELEGATOR = ClassName.get(Delegator.class);

    public static final String FIELD_NAME_DELEGATE_REFERENCE = "delegateReference";
    public static final String PARAM_NAME_DELEGATE           = "delegate";

    public static final String METHOD_NAME_IS_DELEGATE_BOUND = "isDelegateBound";
    public static final String METHOD_NAME_BIND_DELEGATE     = "bindDelegate";
    public static final String METHOD_NAME_UN_BIND_DELEGATE  = "unBindDelegate";
    public static final String METHOD_NAME_GET_DELEGATE      = "getDelegate";

    private final   TypeSpec.Builder classBuilder;
    protected final TypeName         targetClassTypeName;


    public static DelegatorClassBuilder get(TypeSpec.Builder classBuilder,
                                            TypeElement targetClassElement,
                                            boolean useWeakReferenceToDelegate) {
        if (useWeakReferenceToDelegate) {
            return new DelegatorWeakReferenceClassBuilder(classBuilder, targetClassElement);
        } else {
            return new DelegatorStrongReferenceClassBuilder(classBuilder, targetClassElement);
        }
    }


    protected DelegatorClassBuilder(TypeSpec.Builder classBuilder, TypeElement targetClassElement) {
        super();

        this.classBuilder = classBuilder;
        this.targetClassTypeName = TypeName.get(targetClassElement.asType());
    }


    public DelegatorClassBuilder applyClassDefinition() {
        classBuilder.addSuperinterface(
                ParameterizedTypeName.get(CLASS_NAME_DELEGATOR, targetClassTypeName)
        );
        return this;
    }

    public DelegatorClassBuilder applyFields() {
        classBuilder.addField(createFieldDelegateReference());
        return this;
    }

    public DelegatorClassBuilder applyMethods() {
        classBuilder.addMethod(createMethodBindDelegate())
                    .addMethod(createMethodUnBindDelegate())
                    .addMethod(createMethodIsDelegateBound())
                    .addMethod(createMethodGetDelegate());
        return this;
    }


    protected abstract FieldSpec createFieldDelegateReference();


    protected abstract MethodSpec createMethodBindDelegate();

    protected abstract MethodSpec createMethodUnBindDelegate();

    protected MethodSpec createMethodIsDelegateBound(){
        return MethodSpec.methodBuilder(METHOD_NAME_IS_DELEGATE_BOUND)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeName.BOOLEAN)
                         .addCode(CodeBlock.builder()
                                           .addStatement("return this.$L() != null",
                                                         METHOD_NAME_GET_DELEGATE)
                                           .build()
                         )
                         .build();
    }

    protected abstract MethodSpec createMethodGetDelegate();
}