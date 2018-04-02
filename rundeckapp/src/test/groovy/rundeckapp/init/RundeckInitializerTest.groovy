/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
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
package rundeckapp.init

import spock.lang.Specification


class RundeckInitializerTest extends Specification {

    def "ExpandTemplates"() {
        given:
        File sourceTemplates = new File(System.getProperty("user.dir"),"templates")
        File destination = new File(File.createTempFile("test-et","zzz").parentFile,"expandTest-"+UUID.randomUUID().toString().substring(0,8))

        Properties testTemplateProperties = new Properties()

        RundeckInitConfig cfg = new RundeckInitConfig()
        cfg.runtimeConfiguration = testTemplateProperties
        RundeckInitializer initializer = new RundeckInitializer(cfg)

        int sourceFileCount = 0
        sourceTemplates.traverse(type: groovy.io.FileType.FILES) { sourceFileCount++ }

        when:
        initializer.expandTemplatesNonJarMode(sourceTemplates,testTemplateProperties,destination,false)

        then:
        assert destination.exists()
        int filecount
        destination.traverse(type: groovy.io.FileType.FILES) { filecount++ }
        assert filecount == sourceFileCount
    }

}
