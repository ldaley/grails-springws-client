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

import org.springframework.ws.WebServiceMessageFactory

class TemplateConfigBuilder {

	private final $config
	final application

	private TemplateConfigBuilder(grailsApplication, TemplateConfig config) {
		this.application = grailsApplication
		this.$config = config
	}
	
	void template(Class clazz) {
		$config.templateClass = clazz
	}
	
	void mock(Class clazz) {
		$config.mockClass = clazz
	}
	
	void interceptors(Object[] interceptors) {
		$config.interceptors.addAll(interceptors)
	}
	
	void messageFactory(String name) {
		$config.messageFactoryName = name
	}
	
	void messageFactory(WebServiceMessageFactory messageFactory) {
		$config.messageFactory = messageFactory
	}
	
	void schema(String[] schema) {
		$config.schemaResources.addAll(schema.toList())
	}
	
	void validate(boolean flag) {
		$config.validateRequests = flag
		$config.validateResponses = flag
	}
	
	void validate(Map flags) {
		$config.validateRequests = flags.requests == true
		$config.validateResponses = flags.responses == true
	}
	
	void log(boolean flag) {
		log(requests: flag, responses: flag, faults: flag)
	}
	
	void log(Map switches) {
		[requests: "logRequests", responses: "logResponses", faults: "logFaults"].each { k, v ->
			if (switches[k]) {
				$config."$v" = true
			}
		}
	}
	
	static public class InvalidWsClientDSLUsageException extends IllegalStateException {
		InvalidWsClientDSLUsageException(String name, args) {
			super("the wsclient DSL does not support the '$name' method with args ($args)".toString())
		}
	}
	
	def methodMissing(String name, args) {
		throw new InvalidWsClientDSLUsageException(name, args)
	}
	
	static List<TemplateConfig> buildAll(grailsApplication, TemplateConfigFactory configFactory, Closure configs) {
		extractDefinitions(configs).collect { name, definition ->
			if (definition) {
				build(grailsApplication, configFactory.createForName(name), definition)
			} else {
				configFactory.createForName(name)
			}
		}
	}
	
	static TemplateConfig build(grailsApplication, TemplateConfig config, Closure definition) {
		def cloned = definition.clone()
		cloned.delegate = new TemplateConfigBuilder(grailsApplication, config)
		cloned.resolveStrategy = Closure.DELEGATE_FIRST
		cloned()
		config
	}
	
	static extractDefinitions(Closure templates) {
		def definitions = [:]
		def cloned = templates.clone()
		cloned.delegate = new TemplateDefinitionsExtractor(definitions)
		cloned.resolveStrategy = Closure.DELEGATE_FIRST
		cloned()
		definitions
	}

	static private class TemplateDefinitionsExtractor {
		private definitions
		
		TemplateDefinitionsExtractor(Map definitions) {
			this.definitions = definitions
		}
		
		def invokeMethod(String name, args) {
			if (args.size() == 0) {
				definitions[name] = null
			} else if (args.size() == 1 && args[0] instanceof Closure) {
				definitions[name] = args[0]
			} else {
				throw new IllegalStateException("Web Service client definitions must be a method with no args, or a single closure arg (got '$name' with args '${args*.toString()}')")
			}
		}
	}
}