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

import org.eclipse.jetty.server.Server;

eventConfigureJetty = { Server server ->
    println "Jetty server: ${server.class.name}"
    def config = getConfig()
    if (config && config.server) {
        if (config.server.addrealm && config.server.addrealm.classname) {
            def Object o
            try {
                Class clz = Class.forName(config.server.addrealm.classname)
                o = clz.getConstructor(new Class[0]).newInstance(new Object[0])
//                if(o instanceof org.mortbay.jetty.plus.jaas.JAASUserRealm){
//                    def org.mortbay.jetty.plus.jaas.JAASUserRealm realm = (org.mortbay.jetty.plus.jaas.JAASUserRealm)o
//                    realm.setCallbackHandlerClass("org.mortbay.jetty.plus.jaas.callback.DefaultCallbackHandler")
//                }
                try {
                    Class jaasclass = Class.forName("org.eclipse.jetty.jaas.JAASLoginService")

                    if (o.class.isAssignableFrom(jaasclass)) {
                        //configure properties
//                        def JAASLoginService realm = (JAASLoginService) o
                        def realm=o
                        realm.name=config.server.addrealm.name
                        realm.loginModuleName=config.server.addrealm.LoginModuleName
                        server.addBean(realm)
                    }
                }catch (Exception e){
                    System.err.println "Failed to add login service: ${e}"
                    throw e
                }
            } catch (Exception e) {
                System.err.println "Failed to add login service: ${e}"
                throw e
            }

        }
    }
}

getConfig = {
    ClassLoader parent = getClass().getClassLoader()
    GroovyClassLoader loader = new GroovyClassLoader(parent)

    def config

    try {
        def defaultConfigFile = loader.loadClass("DefaultWebrealmsConfig")
        //log.info("Loading default config file: "+defaultConfigFile)
        config = new ConfigSlurper().parse(defaultConfigFile)

        try {
            def appConfigFile = loader.loadClass("WebrealmsConfig")
            //log.info("Found application config file: "+appConfigFile)
            def appConfig = new ConfigSlurper().parse(appConfigFile)
            if (appConfig) {
                //log.info("Merging application config file: "+appConfigFile)
                config = config.merge(appConfig)
            }
        } catch (ClassNotFoundException e) {
            //log.warn("Did not find application config file: "+APP_CONFIG_FILE)
        }
    } catch (ClassNotFoundException e) {
        //log.error("Did not find default config file: "+DEFAULT_CONFIG_FILE)
    }

    config?.webrealms

}
