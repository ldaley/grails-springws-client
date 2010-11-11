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

import org.springframework.ws.WebServiceMessageFactory

class TemplateConfigBuilderSpec extends TemplateSpecUtils {
	
	def grailsApplication = null
	
	def config
	def configs
	
	protected build(Closure definition) {
		config = TemplateConfigBuilder.build(createApplication(), new TemplateConfig(), definition)
	}
	
	protected buildAll(Closure definition) {
		buildAll([:], definition)
	}
	
	
	protected buildAll(Map parameters, Closure definition) {
		def templateConfigFactory = createTemplateConfigFactory(parameters)
		configs = TemplateConfigBuilder.buildAll(grailsApplication, templateConfigFactory, definition)
	}
	
	def "setting template class"() {
		when:
		build {
			template String
		}
		
		then:
		config.templateClass == String
	}
	
	def "setting mock class"() {
		when:
		build {
			mock String
		}
		
		then:
		config.mockClass == String
	}
	
	def "adding interceptors"() {
		given:
		def interceptorObject = new Object()
		
		when:
		build {
			interceptors "anInterceptor", interceptorObject
			interceptors "anotherInterceptor"
		}
		
		then:
		config.interceptors == ["anInterceptor", interceptorObject, "anotherInterceptor"]
	}
	
	def "setting a message factory name"() {
		when:
		build {
			messageFactory "someMessageFactory"
		}
		
		then:
		config.messageFactoryName == "someMessageFactory"
	}
	
	def "setting a message factory instance"() {
		given:
		def instance = [:] as WebServiceMessageFactory
		when:
		build {
			messageFactory instance
		}
		
		then:
		config.messageFactory == instance
	}
	
	def "setting an invalid message factory object"() {
		when:
		build {
			messageFactory 123
		}
		
		then:
		thrown(TemplateConfigBuilder.InvalidWsClientDSLUsageException)
	}
	
	def "setting schema resources"() {
		when:
		build {
			schema "1"
			schema "2", "3"
			schema "4"
		}
		
		then:
		config.schemaResources == ["1", "2", "3", "4"]
	}
	
	def "validating all"() {
		when:
		build {
			validate true
		}
		
		then:
		config.validateRequests == true
		config.validateResponses == true
	}
	
	def "validating specifically"() {
		when:
		build {
			validate(params)
		}
		
		then:
		config.validateRequests == validateRequests
		config.validateResponses == validateResponses
		
		where:
		params                              | validateRequests | validateResponses
		[:]                                 | false            | false
		[requests: false, responses: false] | false            | false
		[requests: true , responses: false] | true             | false
		[requests: false, responses: true ] | false            | true
		[requests: true , responses: true ] | true             | true
		[abc: 123                         ] | false            | false
	}
	
	def "logging all"() {
		when:
		build {
			log true
		}
		
		then:
		config.logRequests == true
		config.logResponses == true
		config.logFaults == true
	}
	
	def "logging specifically"() {
		when:
		build {
			log(params)
		}
		
		then:
		config.logRequests == logRequests
		config.logResponses == logResponses
		config.logFaults == logFaults
		
		where:
		params                                             | logRequests | logResponses | logFaults
		[:]                                                | false       | false        | false
		[requests: false, responses: false, faults: false] | false       | false        | false
		[requests: true , responses: false, faults: false] | true        | false        | false
		[requests: false, responses: true , faults: false] | false       | true         | false
		[requests: true , responses: true , faults: false] | true        | true         | false
		[requests: true , responses: true , faults: true ] | true        | true         | true
		[abc: 123                         ]                | false       | false        | false
	}
	
	def "building multiple clients"() {
		when:
		buildAll {
			c1 {
				mock String
			}
			c2 {
				mock List
			}
		}
		
		then:
		configs*.name == ["c1", "c2"]
		configs*.mockClass == [String, List]
	}
	
	def "building a default client"() {
		when:
		buildAll {
			c1()
		}
		
		then:
		configs*.name == ["c1"]
		configs*.validateRequests == [false]
	}
	
	def "sourcing params"() {
		when:
		buildAll(createConfig { it.clients.c1.validate = true }) {
			c1()
		}
		
		then:
		configs*.name == ["c1"]
		configs*.validateRequests == [true]
	}
	
	def "invalid client definitions calls"() {
		when:
		buildAll {
			foo("bar")
		}
		
		then:
		thrown(IllegalStateException)
	}
	
	def "invalid client definitions"() {
		when:
		buildAll {
			c1 {
				foo("bar")
			}
		}
		
		then:
		thrown(TemplateConfigBuilder.InvalidWsClientDSLUsageException)
	}
	
}