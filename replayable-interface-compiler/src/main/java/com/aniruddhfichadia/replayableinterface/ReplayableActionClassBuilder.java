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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-23
 */
public class ReplayableActionClassBuilder {
    public static final ClassName REPLAYABLE_ACTION = ClassName.get(ReplayableAction.class);

    public static final String METHOD_NAME_REPLAY_ON_TARGET = "replayOnTarget";
    public static final String FIELD_NAME_PARAMS            = "params";
    public static final String PARAM_NAME_TARGET            = "target";

    private final TypeElement targetClassElement;

    private String    constructorArgumentNames;
    private CodeBlock replayOnTargetBody;


    public ReplayableActionClassBuilder(TypeElement targetClassElement) {
        this.targetClassElement = targetClassElement;
    }

    public ReplayableActionClassBuilder constructorArgumentNames(String constructorArgumentNames) {
        this.constructorArgumentNames = constructorArgumentNames;
        return this;
    }

    public ReplayableActionClassBuilder replayOnTargetBody(CodeBlock replayOnTargetBody) {
        this.replayOnTargetBody = replayOnTargetBody;
        return this;
    }


    public TypeSpec build() {
        TypeName targetClassTypeName = TypeName.get(targetClassElement.asType());

        return TypeSpec.anonymousClassBuilder(constructorArgumentNames)
                       .superclass(
                               ParameterizedTypeName.get(REPLAYABLE_ACTION, targetClassTypeName)
                       )
                       .addMethod(MethodSpec.methodBuilder(METHOD_NAME_REPLAY_ON_TARGET)
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addParameter(targetClassTypeName, PARAM_NAME_TARGET)
                                            .addCode(replayOnTargetBody)
                                            .build())
                       .build();
    }
}