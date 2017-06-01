/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.compile.db4o;

import org.codehaus.griffon.compile.core.BaseConstants;
import org.codehaus.griffon.compile.core.MethodDescriptor;

import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedMethod;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedType;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotations;
import static org.codehaus.griffon.compile.core.MethodDescriptor.args;
import static org.codehaus.griffon.compile.core.MethodDescriptor.method;
import static org.codehaus.griffon.compile.core.MethodDescriptor.type;
import static org.codehaus.griffon.compile.core.MethodDescriptor.typeParams;
import static org.codehaus.griffon.compile.core.MethodDescriptor.types;

/**
 * @author Andres Almiray
 */
public interface Db4oAwareConstants extends BaseConstants {
    String OBJECT_CONTAINER_TYPE = "com.db4o.ObjectContainer";
    String OBJECT_CONTAINER_HANDLER_TYPE = "griffon.plugins.db4o.ObjectContainerHandler";
    String OBJECT_CONTAINER_CALLBACK_TYPE = "griffon.plugins.db4o.ObjectContainerCallback";
    String OBJECT_CONTAINER_HANDLER_PROPERTY = "objectContainerHandler";
    String OBJECT_CONTAINER_HANDLER_FIELD_NAME = "this$" + OBJECT_CONTAINER_HANDLER_PROPERTY;

    String METHOD_WITH_OBJECT_CONTAINER = "withDb4o";
    String METHOD_CLOSE_OBJECT_CONTAINER = "closeDb4o";
    String OBJECT_CONTAINER_NAME = "dataSourceName";
    String CALLBACK = "callback";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        method(
            type(VOID),
            METHOD_CLOSE_OBJECT_CONTAINER
        ),
        method(
            type(VOID),
            METHOD_CLOSE_OBJECT_CONTAINER,
            args(annotatedType(types(type(JAVAX_ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),

        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(R),
            typeParams(R),
            METHOD_WITH_OBJECT_CONTAINER,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), OBJECT_CONTAINER_CALLBACK_TYPE, R))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NONNULL)),
            type(R),
            typeParams(R),
            METHOD_WITH_OBJECT_CONTAINER,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), OBJECT_CONTAINER_CALLBACK_TYPE, R))
        )
    };
}
