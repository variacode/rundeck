/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
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

package com.dtolabs.rundeck.core.authorization.providers

import com.dtolabs.rundeck.core.authorization.AuthorizationUtil
import com.dtolabs.rundeck.core.authorization.Validation
import spock.lang.Specification
import spock.lang.Unroll


class YamlProviderSpec extends Specification {

    private static Validation validationForString(String string) {
        YamlProvider.validate(YamlProvider.sourceFromString("test1", string, new Date()))
    }
    private static Validation validationForStringWithProject(String string, String project) {
        YamlProvider.validate(
                YamlProvider.sourceFromString("test1", string, new Date()),
                AuthorizationUtil.projectContext(project)
        )
    }


    def "validate empty list"(){
        when:
        def validation = YamlProvider.validate(Arrays.asList())

        then:
        validation.valid
    }
    def "validate basic ok"(){
        when:
        def validation = validationForString '''
context:
    project: test
by:
    username: elf
    group: jank
for:
    type:
        - allow: '*'
description: blah
id: any string
'''

        then:
        validation.valid
    }
    def "validate no context"(){
        when:
        def validation = validationForString '''
by:
    username: elf
for:
    type:
        - allow: '*'
description: blah
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] == ['Required \'context:\' section was not present']
    }
    def "validate required context, any context"(){
        when:
        def validation = validationForStringWithProject("""
context:
    ${ctx}
by:
    username: elf
for:
    type:
        - allow: '*'
description: blah
""","testproj")

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]']==['Context section should not be specified, it is already set to: {project=testproj}']
        where:
        ctx                    | _
        'application: rundeck' | _
        'project: wrong'       | _
        'project: testproj'    | _
        'project: \'.*\''      | _

    }
    def "validate required context, no context ok"(){
        when:
        def validation = validationForStringWithProject('''
by:
    username: elf
    group: jank
for:
    type:
        - allow: '*\'
description: blah
id: any string
''',"testproj")

        then:
        validation.valid
    }
    def "validate no description"(){
        when:
        def validation = validationForString '''
context:
    project: test
by:
    username: elf
for:
    type:
        - allow: '*'
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] == ['Policy is missing a description']
    }
    def "validate no by"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
for:
    type:
        - allow: '*'
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] == ['Required \'by:\' section was not present']
    }
    def "validate no for"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf

'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] == ['Required \'for:\' section was not present']
    }
    def "validate extraneous section"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for:
    type:
        - allow: '*'
invalid: blah
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'][0].contains(
                'Policy contains invalid keys: [invalid], allowed keys: [by, id, for, context, description]'
        )
    }
    def "validate context invalid entry"(){
        when:
        def validation = validationForString '''
context:
    blab: test
description: wat
by:
    username: elf
for:
    type:
        - allow: '*'
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'].size() == 1
        validation.errors['test1[1]'][0] =~ /Context section should contain only 'application:' or 'project:'/
    }
    def "validate by invalid entry"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    blonk: elf
for:
    type:
        - allow: '*'
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'].size() == 1
        validation.errors['test1[1]'][0] =~ /Cannot create property=blonk/
    }
    def "validate by group: not a string"(){
        when:
        def validation = validationForString """
context:
    project: test
description: wat
by:
    ${section}: 1234
for:
    type:
        - allow: '*'
"""

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] ==
                ['Section \'' + section + ':\' should be a list or a String, but it was: java.lang.Integer']

        where:
        section    | _
        'group'    | _
        'username' | _
    }
    def "validate by section: not a list of strings"(){
        when:
        def validation = validationForString """
context:
    project: test
description: wat
by:
    ${section}: [1234]
for:
    type:
        - allow: '*'
"""

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]']==['Section \''+section+':\' should contain only Strings, but saw a: java.lang.Integer']

        where:
        section    | _
        'group'    | _
        'username' | _
    }
    def "validate for empty"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for: { }
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] == ['Error parsing the policy document: Section \'for:\' cannot be empty']
    }
    def "validate for expect map"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for: [ x , y , z ]
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'][0] =~ /Expected 'for:' section to contain a map, but was sequence/
    }
    def "validate for expect map entry is list"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for:
    blah: { }
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] ==
                ['Error parsing the policy document: Expected \'for: { blah: <...> }\' to be a Seqence, but was [mapping]']
    }
    def "validate type entry requires non-empty"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for:
    blah: [ ]
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] == ['Type rule \'for: { blah: [...] }\' list should not be empty.']
    }
    def "validate type entry requires map"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for:
    blah:
        - [ ]
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'] ==
                ['Error parsing the policy document: Type rule \'for: { blah: [...] }\' entry at index [1] expected a' +
                         ' Map but saw: sequence']
    }
    def "validate type entry requires non-empty map"(){
        when:
        def validation = validationForString '''
context:
    project: test
description: wat
by:
    username: elf
for:
    blah:
        - { }
'''

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]']==['Type rule \'for: { blah: [...] }\' entry at index [1] One of \'allow:\' or \'deny:\' must be present.']
    }
    @Unroll
    def "validate type entry actions expects string or list"(String actionsname,_){
        when:
        def validation = validationForString """
context:
    project: test
description: wat
by:
    username: elf
for:
    blah:
        - ${actionsname}: {}
"""

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]']== [('Type rule \'for: { blah: [...] }\' entry at index [1] Section \'' +
                                                  actionsname +
                                                  ':\' expected a String or a sequence of Strings, but was a java.util.LinkedHashMap')]

        where:
        actionsname | _
        "allow"     | _
        "deny"      | _
    }
    @Unroll
    def "validate type entry actions expects non-empty list"(String actionsname,_){
        when:
        def validation = validationForString """
context:
    project: test
description: wat
by:
    username: elf
for:
    blah:
        - ${actionsname}: []
"""

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]']== [('Type rule \'for: { blah: [...] }\' entry at index [1] Section \'' +
                                                  actionsname +
                ':\' should not be empty')]

        where:
        actionsname | _
        "allow"     | _
        "deny"      | _
    }

    @Unroll
    def "validate type entry #matchname not empty"(String matchname, _) {
        when:
        def validation = validationForString """
context:
    project: test
description: wat
by:
    username: elf
for:
    blah:
        - ${matchname}: {}
          allow: asdf
"""

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]']== [('Type rule \'for: { blah: [...] }\' entry at index [1] Section \'' +
                                                  matchname +
                                                  ':\' should not be empty.')]

        where:
        matchname  | _
        "match"    | _
        "equals"   | _
        "contains" | _
        "subset"   | _
    }
    @Unroll
    def "validate type entry match/equals/contains does not contain allow/deny"(String matchname,String actionsname,_){
        when:
        def validation = validationForString """
context:
    project: test
description: wat
by:
    username: elf
for:
    blah:
        - ${matchname}: { ${actionsname}: asdf }
          allow: asdf
"""

        then:
        !validation.valid
        validation.errors.size()==1
        validation.errors['test1[1]'].size() == 1
        if (matchname == 'contains' && actionsname == 'blee') {
            validation.errors['test1[1]']== [
                    ('Type rule \'for: { blah: [...] }\' entry at index [1] Section \'' +
                            matchname +
                            ':\' can only be applied to \'tags\'.')
            ]
        }else{
            validation.errors['test1[1]']== [('Type rule \'for: { blah: [...] }\' entry at index [1] Section \'' +
                    matchname +
                    ':\' should not contain \'allow:\' or \'deny:\'.')]
        }


        where:
        matchname  | actionsname | _
        "match"    | "allow"     | _
        "match"    | "deny"      | _
        "equals"   | "allow"     | _
        "equals"   | "deny"      | _
        "contains" | "allow"     | _
        "contains" | "deny"      | _
        "contains" | "blee"      | _
    }
    @Unroll
    def "validate multi-policy with empty policy is valid"(){
        when:
        def validation = validationForString """
context:
    project: test
by:
    username: elf
    group: jank
for:
    type:
        - allow: '*'
description: blah
id: any string
---
"""

        then:
        validation.valid
        validation.errors.size()==0
    }

}
