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
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.plugins.db4o.ObjectContainerCallback;
import griffon.plugins.db4o.ObjectContainerFactory;
import griffon.plugins.db4o.ObjectContainerHandler;
import griffon.plugins.db4o.ObjectContainerStorage;
import griffon.plugins.monitor.MBeanManager;
import org.codehaus.griffon.runtime.core.addon.AbstractGriffonAddon;
import org.codehaus.griffon.runtime.jmx.ObjectContainerStorageMonitor;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 */
@Named("db4o")
public class Db4oAddon extends AbstractGriffonAddon {
    @Inject
    private ObjectContainerHandler objectContainerHandler;

    @Inject
    private ObjectContainerFactory objectContainerFactory;

    @Inject
    private ObjectContainerStorage objectContainerStorage;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Override
    public void init(@Nonnull GriffonApplication application) {
        mbeanManager.registerMBean(new ObjectContainerStorageMonitor(metadata, objectContainerStorage));
    }

    public void onStartupStart(@Nonnull GriffonApplication application) {
        for (String dataSourceName : objectContainerFactory.getDataSourceNames()) {
            Map<String, Object> config = objectContainerFactory.getConfigurationFor(dataSourceName);
            if (getConfigValueAsBoolean(config, "connect_on_startup", false)) {
                objectContainerHandler.withDb4o(new ObjectContainerCallback<Object>() {
                    @Override
                    public Object handle(@Nonnull String dataSourceName, @Nonnull ObjectContainer objectContainer) {
                        return null;
                    }
                });
            }
        }
    }

    public void onShutdownStart(@Nonnull GriffonApplication application) {
        for (String dataSourceName : objectContainerFactory.getDataSourceNames()) {
            objectContainerHandler.closeDb4o(dataSourceName);
        }
    }
}
