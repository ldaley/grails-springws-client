/*
 * Copyright 2010 Luke Daley
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

import grails.plugin.springwsclient.template.*

import org.slf4j.LoggerFactory

class SpringwsClientGrailsPlugin {
	def title = "SpringWS Client"
	def version = "0.2-SNAPSHOT"
	def description = 'Integrates the client side aspects of the SpringWS library'
	def author = "Luke Daley"
	def authorEmail = "ld@ldaley.com"
	def documentation = "http://grails.org/plugin/springws-client"

	def grailsVersion = "1.3.5 > *"
	def dependsOn = [:]
	def loadAfter = ['services']

	def pluginExcludes = [
		"grails-app/views/error.gsp",
		"**/grails/plugin/springwsclient/test/**/*"
	]


	private adapters = []
	
	def doWithSpring = {
		def templateBuilder = new TemplateBuilder(delegate)
		def parameterSource = new ApplicationConfigParameterSource(application)
		def defaultTemplateConfig = new TemplateConfig(templateClass: GroovyWebServiceTemplate)
		def templateConfigFactory = new DefaultCloningTemplateConfigFactory(defaultTemplateConfig, parameterSource)
		
		for (serviceClass in application.serviceClasses) {
			if (!ServiceTemplatesAdapter.getDefinitions(serviceClass)) {
				if (log.debugEnabled) {
					log.debug "service '$serviceClass.name' does not define any wsclients"
				}
				continue
			}

			if (log.infoEnabled) {
				log.info "configuring ws clients for '$serviceClass.name'"
			}
			
			configureClientsForServiceClass(serviceClass, application, templateConfigFactory, templateBuilder, adapters)
		}
	}

	def doWithDynamicMethods = { ctx ->
		for (adapter in adapters) {
			for (templateConfig in adapter.templateConfigs) {
				def beanName = templateConfig.beanName
				adapter.serviceClass.metaClass."get${beanName.capitalize()}" = { it }.curry(ctx.getBean(beanName))
			}
		}
	}
	
	void configureClientsForServiceClass(serviceClass, grailsApplication, templateConfigFactory, builder, adapters) {
		def adapter = new ServiceTemplatesAdapter(serviceClass, grailsApplication, templateConfigFactory)
		adapters << adapter
		adapter.buildWith(builder)
	}
	
	private static final log = LoggerFactory.getLogger('grails.plugin.springwsclient.SpringwsClientGrailsPlugin')
}