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

webrealms{
    /*
    loginconfig: This corresponds to the <login-config> element in web.xml.
        authmethod=FORM/BASIC
        realmname=<your realm name>
    in the case of FORM authmethod, use the following properties as well:
        loginpage=/path/to/login/form
        errorpage=/path/to/errorpage
     */
//    loginconfig{
//        authmethod= 'FORM'
//        realmname= 'rundeckrealm'
//        loginpage='/user/login'
//        errorpage='/user/error'
//    }
    /*
    securityconstraint allows you to specify the <security-constraint> elements for web.xml
            X.urlpattern = Z
            corresponds to:
                <web-resource-collection> <web-resource-name>X</web-resource-name><url-pattern>Z</url-pattern></web-resource-collection>

        you can also specify:
            X.authconstraint.rolename=Y
            this will add the <auth-constraint><role-name>Y</role-name></auth-constraint> inside the <security-constraint>

     */
//    securityconstraint{
//        Login.urlpattern='/user/*'
//        Images.urlpattern='/images/*'
//        CSS.urlpattern='/css/*'
//        JS.urlpattern='/js/*'
//        Feed.urlpattern='/feed/*'
//        all{
//            urlpattern='/*'
//            authconstraint{
//                rolename="*"
//            }
//        }
//    }

    /**
     *  <New class="org.mortbay.jetty.plus.jaas.JAASUserRealm">
                <Set name="name">rundeckrealm</Set>
                <Set name="LoginModuleName">rundecklogin</Set>
            </New>

     */
//    server{
//        addrealm{
//            classname="org.mortbay.jetty.plus.jaas.JAASUserRealm"
//            name="rundeckrealm"
//            LoginModuleName="rundecklogin"
//        }
//    }

}