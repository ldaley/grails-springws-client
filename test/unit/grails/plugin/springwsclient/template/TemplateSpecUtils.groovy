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

package grails.plugin.springwsclient.template

import spock.lang.*
import grails.plugin.spock.*

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsServiceClass

class TemplateSpecUtils extends UnitSpec {

	protected createApplication(Map impl = [:]) {
		if (!impl.containsKey('config')) {
			impl.config = new ConfigObject()
		}
		
		impl as GrailsApplication
	}

	protected createApplicationWithConfig(Closure definition) {
		createApplication(getConfig: { it }.curry(createConfig(definition)))
	}
	
	protected createConfig(Closure definition) {
		def co = new ConfigObject()
		definition(co)
		co
	}
	
	protected createServiceClass(Map impl = [:]) {
		impl as GrailsServiceClass
	}
	
	protected createServiceClassWithWsClients(wsclients) {
		createServiceClass(
			getPropertyValue: { 
				assert it == "wsclients"
				wsclients 
			}
		)
	}

	protected createTemplateConfigFactory(TemplateConfig templateConfig = new TemplateConfig(), Closure configSetup = {}) {
		def grailsApplication = createApplicationWithConfig { configSetup(it.springwsclient) }
		def parameterSource = new ApplicationConfigParameterSource(grailsApplication)
		def templateConfigFactory = new DefaultCloningTemplateConfigFactory(templateConfig, parameterSource)
	}

	protected createTemplateConfigFactory(Closure configSetup) {
		createTemplateConfigFactory(new TemplateConfig(), configSetup)
	}

	protected createTemplateConfigFactory(Map configSetup, TemplateConfig templateConfig = new TemplateConfig()) {
		createTemplateConfigFactory(templateConfig) {
			it.putAll(configSetup)
		}
	}
	
	protected templateConfig(Map values) {
		new TemplateConfig(values)
	}

	protected toConfigObject(Map params) {
		def co = new ConfigObject()
		co.putAll(params)
		co
	}
	
}