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

import grails.plugin.springwsclient.destination.ConstantDestinationProvider

import grails.plugin.springwsclient.marshalling.MarkupBuilderMarshaller
import grails.plugin.springwsclient.marshalling.XmlSlurperUnmarshaller
import grails.plugin.springwsclient.interceptor.LoggingInterceptor

import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor

class TemplateBuilder {

	final beanBuilder
	
	TemplateBuilder(beanBuilder) {
		this.beanBuilder = beanBuilder
	}

	void build(templateConfig) {
		beanBuilder.with {
			def destinationProvider = null
			
			if (templateConfig.shouldCreateMock) {
				if (templateConfig.mockClass == null) {
					throw new IllegalStateException("Cannot configure mock for ${templateConfig.beanName} as no mock implementation class has been specified")
				}
				
				"$templateConfig.mockBeanName"(templateConfig.mockClass) {
					it.autowire = true
				}
				
				destinationProvider = ref(templateConfig.mockBeanName)
			} else {
				def destinationParameter = templateConfig.destinationParameter
				if (destinationParameter) {
					destinationProvider = new ConstantDestinationProvider(destinationParameter)
				}
			}
			
			"$templateConfig.beanName"(templateConfig.templateClass) {
				
				if (destinationProvider) {
					delegate.destinationProvider = destinationProvider
				} else if (templateConfig.destinationProviderName) {
					delegate.destinationProvider = ref(templateConfig.destinationProviderName)
				}
				
				if (templateConfig.marshallerName) {
					marshaller = ref(templateConfig.marshallerName)
				} else {
					marshaller = this.createDefaultMarshaller() 
				}

				if (templateConfig.unmarshallerName) {
					unmarshaller = ref(templateConfig.unmarshallerName)
				} else {
					unmarshaller = this.createDefaultUnmarshaller() 
				}
				
				if (templateConfig.messageFactory) {
					messageFactory = templateConfig.messageFactory
				} else if (templateConfig.messageFactoryName) {
					messageFactory = ref(templateConfig.messageFactoryName)
				}
				
				def interceptors = []
				
				// If no schemas were defined, this will be an empty list
				def schemas = templateConfig.schemaResources.collect { parentCtx.getResource(it) }

				// interceptors are executed in reverse on responses, so validating responses before logging
				// in the chain actually means that logging happens before validation on responses which is
				// what we want here.
				if (schemas && templateConfig.validateResponses) {
					interceptors << this.createValidatingInterceptor(schemas, false, true)
				}

				if (templateConfig.shouldLog) {
					interceptors << new LoggingInterceptor(
						templateConfig.logName, 
						templateConfig.logRequests, 
						templateConfig.logResponses, 
						templateConfig.logFaults
					)
				}

				// We want to validate after logging so we can see the message if need be
				if (schemas && templateConfig.validateRequests) {
					interceptors << this.createValidatingInterceptor(schemas, true, false)
				}
				
				if (interceptors) {
					delegate.interceptors = interceptors as ClientInterceptor[]
				}
			}
		}
	}
	
	protected createValidatingInterceptor(schemas, boolean forRequests, boolean forResponses) {
		def interceptor = new PayloadValidatingInterceptor()
		interceptor.schemas = schemas
		interceptor.validateRequest = forRequests
		interceptor.validateResponse = forResponses
		interceptor.afterPropertiesSet()
		interceptor
	}
	
	protected createDefaultMarshaller() {
		new MarkupBuilderMarshaller()
	}
	
	protected createDefaultUnmarshaller() {
		new XmlSlurperUnmarshaller()
	}
}