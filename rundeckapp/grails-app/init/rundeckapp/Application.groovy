package rundeckapp

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

@ComponentScan
class Application extends GrailsAutoConfiguration implements EnvironmentAware {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        def configBase = System.getenv('RUNDECK_CONFIG_LOCATION') ?: System.getProperty('rundeck.config.location') ?: "/home/parth/git/rundeck/rundeckapp/rundeck-config"

        boolean configLoaded = false

        def ymlConfig = new File(configBase + '.yml')
        if(ymlConfig.exists()) {
            println "Loading external configuration from YAML: ${ymlConfig.absolutePath}"
            Resource resourceConfig = new FileSystemResource(ymlConfig)
            YamlPropertiesFactoryBean ypfb = new YamlPropertiesFactoryBean()
            ypfb.setResources(resourceConfig)
            ypfb.afterPropertiesSet()
            Properties properties = ypfb.getObject()
            environment.propertySources.addFirst(new PropertiesPropertySource("externalYamlConfig", properties))
            println "External YAML configuration loaded."
            configLoaded = true
        }

        def groovyConfig = new File(configBase + '.groovy')
        if(groovyConfig.exists()) {
            println "Loading external configuration from Groovy: ${groovyConfig.absolutePath}"
            def config = new ConfigSlurper().parse(groovyConfig.toURL())
            environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))
            println "External Groovy configuration loaded."
            configLoaded = true
        }

        if(!configLoaded) {
            println "External config could not be found, checked ${ymlConfig.absolutePath} and ${groovyConfig.absolutePath}"
        }
    }

    /*@Bean(initMethod = "init")
    public void initMethodToCheckExecution() {
        println '----------------------yes it gets executed first--------------------'
    }*/
}