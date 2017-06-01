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
package org.codehaus.griffon.runtime.db4o;

import griffon.core.Configuration;
import griffon.core.addon.GriffonAddon;
import griffon.core.injection.Module;
import griffon.plugins.db4o.ObjectContainerFactory;
import griffon.plugins.db4o.ObjectContainerHandler;
import griffon.plugins.db4o.ObjectContainerStorage;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.codehaus.griffon.runtime.util.ResourceBundleProvider;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;
import java.util.ResourceBundle;

import static griffon.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
@Named("db4o")
@ServiceProviderFor(Module.class)
public class Db4oModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(ResourceBundle.class)
            .withClassifier(named("db4o"))
            .toProvider(new ResourceBundleProvider("Db4o"))
            .asSingleton();

        bind(Configuration.class)
            .withClassifier(named("db4o"))
            .to(DefaultDb4oConfiguration.class)
            .asSingleton();

        bind(ObjectContainerStorage.class)
            .to(DefaultObjectContainerStorage.class)
            .asSingleton();

        bind(ObjectContainerFactory.class)
            .to(DefaultObjectContainerFactory.class)
            .asSingleton();

        bind(ObjectContainerHandler.class)
            .to(DefaultObjectContainerHandler.class)
            .asSingleton();

        bind(GriffonAddon.class)
            .to(Db4oAddon.class)
            .asSingleton();
        // end::bindings[]
    }
}
