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

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import griffon.core.Configuration;
import griffon.core.GriffonApplication;
import griffon.core.injection.Injector;
import griffon.plugins.db4o.Db4oBootstrap;
import griffon.plugins.db4o.ObjectContainerFactory;
import org.codehaus.griffon.runtime.core.storage.AbstractObjectFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultObjectContainerFactory extends AbstractObjectFactory<ObjectContainer> implements ObjectContainerFactory {
    private static final String ERROR_DATASOURCE_BLANK = "Argument 'dataSourceName' must not be blank";

    private final Set<String> dataSourceNames = new LinkedHashSet<>();

    @Inject
    private Injector injector;

    @Inject
    public DefaultObjectContainerFactory(@Nonnull @Named("db4o") Configuration configuration, @Nonnull GriffonApplication application) {
        super(configuration, application);
        dataSourceNames.add(KEY_DEFAULT);

        if (configuration.containsKey(getPluralKey())) {
            Map<String, Object> db4os = (Map<String, Object>) configuration.get(getPluralKey());
            dataSourceNames.addAll(db4os.keySet());
        }
    }

    @Nonnull
    @Override
    public Set<String> getDataSourceNames() {
        return dataSourceNames;
    }

    @Nonnull
    @Override
    public Map<String, Object> getConfigurationFor(@Nonnull String dataSourceName) {
        requireNonBlank(dataSourceName, ERROR_DATASOURCE_BLANK);
        return narrowConfig(dataSourceName);
    }

    @Nonnull
    @Override
    protected String getSingleKey() {
        return "dataSource";
    }

    @Nonnull
    @Override
    protected String getPluralKey() {
        return "dataSources";
    }

    @Nonnull
    @Override
    public ObjectContainer create(@Nonnull String name) {
        requireNonBlank(name, ERROR_DATASOURCE_BLANK);
        Map<String, Object> config = narrowConfig(name);

        if (config.isEmpty()) {
            throw new IllegalArgumentException("DataSource '" + config + "' is not configured.");
        }

        event("Db4oConnectStart", asList(name, config));

        ObjectContainer objectContainer = createObjectContainer(config, name);

        for (Object o : injector.getInstances(Db4oBootstrap.class)) {
            ((Db4oBootstrap) o).init(name, objectContainer);
        }

        event("Db4oConnectEnd", asList(name, config, objectContainer));

        return objectContainer;
    }

    @Override
    public void destroy(@Nonnull String name, @Nonnull ObjectContainer instance) {
        requireNonBlank(name, ERROR_DATASOURCE_BLANK);
        requireNonNull(instance, "Argument 'instance' must not be null");
        Map<String, Object> config = narrowConfig(name);

        if (config.isEmpty()) {
            throw new IllegalArgumentException("DataSource '" + config + "' is not configured.");
        }

        event("Db4oDisconnectStart", asList(name, config, instance));

        for (Object o : injector.getInstances(Db4oBootstrap.class)) {
            ((Db4oBootstrap) o).destroy(name, instance);
        }

        destroyObjectContainer(config, instance);

        event("Db4oDisconnectEnd", asList(name, config));
    }

    @Nonnull
    private ObjectContainer createObjectContainer(@Nonnull Map<String, Object> config, @Nonnull String name) {
        File dbfile = resolveDBFile(config);
        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        event("Db4oConfigurationSetup", asList(name, config, configuration));
        return Db4oEmbedded.openFile(configuration, dbfile.getAbsolutePath());
    }

    private void destroyObjectContainer(@Nonnull Map<String, Object> config, @Nonnull ObjectContainer container) {
        container.close();

        boolean delete = getConfigValueAsBoolean(config, "delete", false);

        if (delete) {
            File dbfile = resolveDBFile(config);
            dbfile.delete();
        }
    }

    @Nonnull
    private File resolveDBFile(@Nonnull Map<String, Object> config) {
        String dbfileName = getConfigValueAsString(config, "name", "db.yarv");
        File dbfile = new File(dbfileName);
        if (!dbfile.isAbsolute()) {
            dbfile = new File(System.getProperty("user.dir"), dbfile.getPath());
        }
        dbfile.getParentFile().mkdirs();
        return dbfile;
    }
}
