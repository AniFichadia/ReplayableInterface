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
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import static com.google.auto.common.MoreElements.getPackage;


/**
 * @author Aniruddh Fichadia
 * @date 2017-01-18
 */
@AutoService(Processor.class)
public class ReplayableInterfaceProcessor
        extends AbstractProcessor {
    private static final boolean IS_TEST = false;

    public static final String PACKAGE_REPLAYABLE_INTERFACE = "com.aniruddhfichadia.replayableinterface";

    public static final ClassName REPLAY_STRATEGY = ClassName.get(PACKAGE_REPLAYABLE_INTERFACE,
                                                                  "ReplayStrategy");
    public static final ClassName STRING          = ClassName.get("java.lang", "String");
    public static final ClassName WEAK_REFERENCE  = ClassName.get("java.lang.ref", "WeakReference");


    private Filer filer;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }


    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();

        annotations.add(ReplayableInterface.class);

        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        // Process each @ReplayableInterface element.
        for (Element element : env.getElementsAnnotatedWith(ReplayableInterface.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }

            List<String> warnings = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            if (ElementKind.INTERFACE.equals(element.getKind())) {
                TypeElement enclosingElement = findEnclosingTypeElement(element);

                String packageName = getPackage(enclosingElement).getQualifiedName().toString();
                String className = enclosingElement.getQualifiedName().toString().substring(
                        packageName.length() + 1);

                ClassName targetClassName = ClassName.get(packageName, className);
                ClassName replayableClassName = ClassName.get(packageName,
                                                              "Replayable" +
                                                                      className.replace('.', '$'));

                ReplayableInterface replayableInterface = element.getAnnotation(
                        ReplayableInterface.class
                );
                ReplayStrategy defaultReplyStrategy = replayableInterface.value();
                ReplayType replayType = replayableInterface.replayType();
                boolean clearAfterReplaying = replayableInterface.clearAfterReplaying();

                TypeSpec.Builder classBuilder =
                        TypeSpec.classBuilder(replayableClassName)
                                .addModifiers(Modifier.PUBLIC)
                                .addJavadoc(
                                        "ReplayableInterface implementation of {@link $T}.\n",
                                        targetClassName);

                ReplayableInterfaceTargetVisitor replayableInterfaceTargetVisitor =
                        new ReplayableInterfaceTargetVisitor(classBuilder, targetClassName, element,
                                                             replayType, defaultReplyStrategy)
                                .applyClassDefinition()
                                .applyMethods();
                warnings.addAll(replayableInterfaceTargetVisitor.getWarnings());
                errors.addAll(replayableInterfaceTargetVisitor.getErrors());


                new DelegatorVisitor(classBuilder, targetClassName)
                        .applyClassDefinition()
                        .applyFields()
                        .applyMethods();

                new ReplaySourceVisitor(classBuilder, targetClassName, clearAfterReplaying)
                        .applyClassDefinition()
                        .applyFields()
                        .applyMethods();


                try {
                    JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                                                .addFileComment(
                                                        "This code was generated by a tool " +
                                                                "(ReplayableInterface, not a human tool :D)"
                                                )
                                                .build();
                    javaFile.writeTo(filer);

                    if (IS_TEST) {
                        javaFile.writeTo(System.out);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                errors.add(String.format("%s is not supported. %s must be an interface.",
                                         element.getSimpleName(), element.getSimpleName()));
            }

            for (String warning : warnings) {
                warning(element, warning);
            }

            for (String error : errors) {
                error(element, error);
            }
        }

        return false;
    }

    public static TypeElement findEnclosingTypeElement(Element e) {
        while (e != null && !(e instanceof TypeElement)) {
            e = e.getEnclosingElement();
        }
        return TypeElement.class.cast(e);
    }


    //region Logging
    private void error(Element element, String message, Object... args) {
        printMessage(Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Kind.NOTE, element, message, args);
    }

    private void warning(Element element, String message, Object... args) {
        printMessage(Kind.WARNING, element, message, args);
    }

    private void printMessage(Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }
    //endregion
}