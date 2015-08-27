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
package griffon.plugins.db4o

import com.db4o.ObjectContainer
import griffon.core.CallableWithArgs
import griffon.core.GriffonApplication
import griffon.core.test.GriffonUnitRule
import griffon.inject.BindTo
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@Unroll
class Db4oSpec extends Specification {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private ObjectContainerHandler objectContainerHandler

    @Inject
    private GriffonApplication application

    void 'Open and close default dataSource'() {
        given:
        List eventNames = [
            'Db4oConnectStart', 'Db4oConfigurationSetup', 'Db4oConnectEnd',
            'Db4oDisconnectStart', 'Db4oDisconnectEnd'
        ]
        List events = []
        eventNames.each { name ->
            application.eventRouter.addEventListener(name, { Object... args ->
                events << [name: name, args: args]
            } as CallableWithArgs)
        }

        when:
        objectContainerHandler.withDb4o { String dataSourceName, ObjectContainer objectContainer ->
            true
        }
        objectContainerHandler.closeDb4o()
        // second call should be a NOOP
        objectContainerHandler.closeDb4o()

        then:
        events.size() == 5
        events.name == eventNames
    }

    void 'Connect to default dataSource'() {
        expect:
        objectContainerHandler.withDb4o { String dataSourceName, ObjectContainer objectContainer ->
            dataSourceName == 'default' && objectContainer
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        objectContainerHandler.withDb4o { String dataSourceName, ObjectContainer objectContainer -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        objectContainerHandler.withDb4o { String dataSourceName, ObjectContainer objectContainer -> }
        objectContainerHandler.closeDb4o()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name dataSource'() {
        expect:
        objectContainerHandler.withDb4o(name) { String dataSourceName, ObjectContainer objectContainer ->
            dataSourceName == name && objectContainer
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus dataSource name (#name) results in error'() {
        when:
        objectContainerHandler.withDb4o(name) { String dataSourceName, ObjectContainer objectContainer ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people dataSource'() {
        when:
        List peopleIn = objectContainerHandler.withDb4o('people') { String dataSourceName, ObjectContainer objectContainer ->
            [[id: '1', name: 'Danno', lastname: 'Ferrin'],
             [id: '2', name: 'Andres', lastname: 'Almiray'],
             [id: '3', name: 'James', lastname: 'Williams'],
             [id: '4', name: 'Guillaume', lastname: 'Laforge'],
             [id: '5', name: 'Jim', lastname: 'Shingler'],
             [id: '6', name: 'Alexander', lastname: 'Klein'],
             [id: '7', name: 'Rene', lastname: 'Groeschke']].collect { data ->
                new Person(data)
            }.each { person -> objectContainer.store(person) }
        }

        List peopleOut = objectContainerHandler.withDb4o('people') { String dataSourceName, ObjectContainer objectContainer ->
            println objectContainer.query(Person).size()
            objectContainer.query(Person).collect { it }
        }

        then:
        peopleIn == peopleOut

        cleanup:
        objectContainerHandler.closeDb4o()
    }

    @BindTo(Db4oBootstrap)
    private TestDb4oBootstrap bootstrap = new TestDb4oBootstrap()
}
