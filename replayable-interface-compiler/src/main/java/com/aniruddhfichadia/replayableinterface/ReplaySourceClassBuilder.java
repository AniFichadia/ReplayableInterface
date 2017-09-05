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
import com.squareup.javapoet.TypeSpec.Builder;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.aniruddhfichadia.replayableinterface.ReplayableActionClassBuilder.METHOD_NAME_REPLAY_ON_TARGET;
import static com.aniruddhfichadia.replayableinterface.ReplayableActionClassBuilder.REPLAYABLE_ACTION;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-21
 */
public class ReplaySourceClassBuilder {
    public static final ClassName CLASS_NAME_REPLAY_SOURCE = ClassName.get(ReplaySource.class);

    public static final ClassName CLASS_NAME_STRING          = ClassName.get(String.class);
    public static final ClassName CLASS_NAME_LINKED_HASH_MAP = ClassName.get(LinkedHashMap.class);
    public static final ClassName CLASS_NAME_ENTRY           = ClassName.get(Map.Entry.class);

    public static final String FIELD_NAME_ACTIONS                = "actions";
    public static final String FIELD_NAME_CLEAR_AFTER_REPLAYING  = "clearAfterReplaying";
    public static final String METHOD_NAME_ADD_REPLAYABLE_ACTION = "addReplayableAction";
    public static final String METHOD_NAME_REPLAY                = "replay";
    public static final String PARAM_NAME_KEY                    = "key";
    public static final String PARAM_NAME_ACTION                 = "action";
    public static final String PARAM_NAME_TARGET                 = "target";

    private final TypeSpec.Builder classBuilder;
    private final TypeName         targetClassTypeName;
    private final boolean          clearAfterReplaying;

    private final ClassName typeKey = CLASS_NAME_STRING;
    private final ParameterizedTypeName typeValue;


    public ReplaySourceClassBuilder(Builder classBuilder, TypeElement targetClassElement,
                                    boolean clearAfterReplaying) {
        super();

        this.classBuilder = classBuilder;
        this.targetClassTypeName = TypeName.get(targetClassElement.asType());
        this.typeValue = ParameterizedTypeName.get(REPLAYABLE_ACTION, targetClassTypeName);
        this.clearAfterReplaying = clearAfterReplaying;
    }


    public ReplaySourceClassBuilder applyClassDefinition() {
        classBuilder.addSuperinterface(
                ParameterizedTypeName.get(CLASS_NAME_REPLAY_SOURCE, targetClassTypeName)
        );
        return this;
    }

    public ReplaySourceClassBuilder applyFields() {
        classBuilder.addField(createFieldActions());
        classBuilder.addField(createFieldClearAfterReplaying());

        return this;
    }

    public ReplaySourceClassBuilder applyMethods() {
        classBuilder.addMethod(createMethodAddReplayableAction());
        classBuilder.addMethod(createMethodReplay());
        return this;
    }


    private FieldSpec createFieldActions() {
        return FieldSpec.builder(
                ParameterizedTypeName.get(CLASS_NAME_LINKED_HASH_MAP, typeKey, typeValue),
                FIELD_NAME_ACTIONS)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer(CodeBlock.of("new LinkedHashMap<>()"))
                        .build();
    }

    private FieldSpec createFieldClearAfterReplaying() {
        return FieldSpec.builder(TypeName.BOOLEAN, FIELD_NAME_CLEAR_AFTER_REPLAYING)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build();
    }


    private MethodSpec createMethodAddReplayableAction() {
        return MethodSpec.methodBuilder(METHOD_NAME_ADD_REPLAYABLE_ACTION)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .addParameter(CLASS_NAME_STRING, PARAM_NAME_KEY)
                         .addParameter(
                                 ParameterizedTypeName.get(REPLAYABLE_ACTION, targetClassTypeName),
                                 PARAM_NAME_ACTION)
                         .addCode(CodeBlock.builder()
                                           .addStatement("this.$L.remove($L)", FIELD_NAME_ACTIONS,
                                                         PARAM_NAME_KEY)
                                           .addStatement("this.$L.put($L, $L)", FIELD_NAME_ACTIONS,
                                                         PARAM_NAME_KEY, PARAM_NAME_ACTION)
                                           .build())
                         .build();
    }

    private MethodSpec createMethodReplay() {
        CodeBlock.Builder replayMethodBody =
                CodeBlock.builder()
                         .beginControlFlow("for ($T entry : $L.entrySet())",
                                           ParameterizedTypeName.get(CLASS_NAME_ENTRY, typeKey,
                                                                     typeValue),
                                           FIELD_NAME_ACTIONS)
                         .addStatement("entry.getValue().$L($L)", METHOD_NAME_REPLAY_ON_TARGET,
                                       PARAM_NAME_TARGET)
                         .endControlFlow();

        replayMethodBody.beginControlFlow("if($L)", FIELD_NAME_CLEAR_AFTER_REPLAYING)
                        .addStatement("$L.clear()", FIELD_NAME_ACTIONS)
                        .endControlFlow();

        return MethodSpec.methodBuilder(METHOD_NAME_REPLAY)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .addParameter(targetClassTypeName, PARAM_NAME_TARGET)
                         .addCode(replayMethodBody.build())
                         .build();
    }
}