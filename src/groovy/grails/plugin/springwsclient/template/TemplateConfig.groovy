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

/**
 * Struct like object that describes how a template is to be configured.
 */
class TemplateConfig implements Cloneable {
	
	static public final BEAN_NAME_SUFFIX = "WsClient"
	static public final MOCK_BEAN_NAME_SUFFIX = "WsMock"
	
	String name
	Class templateClass
	List interceptors = []
	String[] messageSenderNames
	
	String messageFactoryName
	WebServiceMessageFactory messageFactory
	
	String marshallerName
	String unmarshallerName
	String destinationProviderName
	
	Class mockClass
	
	boolean validateRequests = false
	boolean validateResponses = false
	List<String> schemaResources = []
	
	boolean logRequests = false
	boolean logResponses = false
	boolean logFaults = false
	
	Map parameters
	
	String getBeanName() {
		name + BEAN_NAME_SUFFIX
	}
	
	String getMockBeanName() {
		name + MOCK_BEAN_NAME_SUFFIX
	}
	
	boolean isShouldCreateMock() {
		parameters?.mock == true
	}
	
	boolean isShouldLog() {
		logRequests || logResponses || logFaults
	}

	boolean isShouldValidate() {
		(this.isValidateRequests() || this.isValidateResponses()) && !schemaResources.empty 
	}
	
	boolean isValidateRequests() {
		this.validateRequests || (parameters?.validateRequests == true || parameters?.validate == true)
	}

	boolean isValidateResponses() {
		this.validateResponses || (parameters?.validateResponses == true || parameters?.validate == true)
	}
	
	String getLogName() {
		"grails.plugin.springwsclient.client." + name
	}
	
	URI getDestinationParameter() {
		def uri = null
		def destinationParameter = parameters?.destination
		if (destinationParameter instanceof String) {
			uri = new URI(destinationParameter)
		} else if (destinationParameter instanceof URI) {
			uri = destinationParameter
		}
		
		uri
	}
}