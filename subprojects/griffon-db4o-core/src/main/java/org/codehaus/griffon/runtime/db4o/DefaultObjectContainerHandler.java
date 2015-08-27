/*
 * Copyright 2014-2015 the original author or authors.
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
package org.codehaus.griffon.runtime.db4o;

import com.db4o.ObjectContainer;
import griffon.plugins.db4o.ObjectContainerCallback;
import griffon.plugins.db4o.ObjectContainerFactory;
import griffon.plugins.db4o.ObjectContainerHandler;
import griffon.plugins.db4o.ObjectContainerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultObjectContainerHandler implements ObjectContainerHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultObjectContainerHandler.class);
    private static final String ERROR_DATASOURCE_NAME_BLANK = "Argument 'dataSourceName' must not be blank";
    private static final String ERROR_OBJECT_CONTAINER_NULL = "Argument 'objectContainer' must not be null";
    private static final String ERROR_CALLBACK_NULL = "Argument 'callback' must not be null";

    private final ObjectContainerFactory objectContainerFactory;
    private final ObjectContainerStorage objectContainerStorage;

    @Inject
    public DefaultObjectContainerHandler(@Nonnull ObjectContainerFactory objectContainerFactory, @Nonnull ObjectContainerStorage objectContainerStorage) {
        this.objectContainerFactory = requireNonNull(objectContainerFactory, "Argument 'objectContainerFactory' must not be null");
        this.objectContainerStorage = requireNonNull(objectContainerStorage, "Argument 'objectContainerStorage' must not be null");
    }

    @Nullable
    @Override
    public <R> R withDb4o(@Nonnull ObjectContainerCallback<R> callback) {
        return withDb4o(DefaultObjectContainerFactory.KEY_DEFAULT, callback);
    }

    @Nullable
    @Override
    public <R> R withDb4o(@Nonnull String dataSourceName, @Nonnull ObjectContainerCallback<R> callback) {
        requireNonBlank(dataSourceName, ERROR_DATASOURCE_NAME_BLANK);
        requireNonNull(callback, ERROR_CALLBACK_NULL);

        ObjectContainer objectContainer = getObjectContainer(dataSourceName);
        return doWithObjectContainer(dataSourceName, objectContainer, callback);
    }

    @Nullable
    @SuppressWarnings("ThrowFromFinallyBlock")
    static <R> R doWithObjectContainer(@Nonnull String dataSourceName, @Nonnull ObjectContainer objectContainer, @Nonnull ObjectContainerCallback<R> callback) {
        requireNonBlank(dataSourceName, ERROR_DATASOURCE_NAME_BLANK);
        requireNonNull(objectContainer, ERROR_OBJECT_CONTAINER_NULL);
        requireNonNull(callback, ERROR_CALLBACK_NULL);

        LOG.debug("Executing statements on objectContainer '{}'", dataSourceName);
        return callback.handle(dataSourceName, objectContainer);
    }

    @Override
    public void closeDb4o() {
        closeDb4o(DefaultObjectContainerFactory.KEY_DEFAULT);
    }

    @Override
    public void closeDb4o(@Nonnull String dataSourceName) {
        ObjectContainer objectContainer = objectContainerStorage.get(dataSourceName);
        if (objectContainer != null) {
            objectContainerFactory.destroy(dataSourceName, objectContainer);
            objectContainerStorage.remove(dataSourceName);
        }
    }

    @Nonnull
    private ObjectContainer getObjectContainer(@Nonnull String dataSourceName) {
        ObjectContainer objectContainer = objectContainerStorage.get(dataSourceName);
        if (objectContainer == null) {
            objectContainer = objectContainerFactory.create(dataSourceName);
            objectContainerStorage.set(dataSourceName, objectContainer);
        }
        return objectContainer;
    }
}
