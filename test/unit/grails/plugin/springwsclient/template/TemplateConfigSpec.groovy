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

class TemplateConfigSpec extends TemplateSpecUtils {

	def "should log or not"() {
		expect:
		templateConfig(logRequests: logRequests, logResponses: logResponses, logFaults: logFaults).shouldLog == should
		
		where:
		logRequests | logResponses | logFaults | should
		false       | false        | false     | false
		true        | false        | false     | true
		false       | true         | false     | true
		false       | false        | true      | true
		true        | true         | true      | true
	}
	
	def "should validate or not"() {
		expect:
		templateConfig(
			parameters: toConfigObject(parameters), 
			validateRequests: validateRequests, 
			validateResponses: validateResponses,
			schemaResources: schemaResources
		).shouldValidate == should
		
		where:
		validateRequests | validateResponses | parameters                                           | schemaResources               | should
		false            | false             | [:]                                                  | []                            | false
		true             | false             | [:]                                                  | []                            | false
		false            | true              | [:]                                                  | []                            | false
		true             | false             | [validateResponses: true , validateRequests: false]  | []                            | false
		true             | false             | [validateResponses: false, validateRequests: true ]  | []                            | false
		false            | false             | [validateResponses: false, validateRequests: false]  | ["abc"]                       | false
		true             | true              | [:                                                ]  | ["abc"]                       | true
		false            | false             | [validateResponses: true , validateRequests: false]  | ["abc"]                       | true
		false            | false             | [validateResponses: false, validateRequests: true ]  | ["abc"]                       | true
		false            | false             | [validate: true                                   ]  | ["abc"]                       | true 
	}
	
	def "should validate request and|or response"() {
		expect:
		templateConfig(parameters: toConfigObject(parameters), validateRequests: validateRequests,  validateResponses: validateResponses).validateRequests == shouldValidateRequest
		templateConfig(parameters: toConfigObject(parameters), validateRequests: validateRequests,  validateResponses: validateResponses).validateResponses == shouldValidateResponse
		
		where:
		validateRequests | validateResponses | parameters                                           | shouldValidateRequest | shouldValidateResponse
		false            | false             | [:]                                                  | false                 | false
		true             | false             | [:]                                                  | true                  | false
		false            | true              | [:]                                                  | false                 | true 
		true             | false             | [validateResponses: true , validateRequests: false]  | true                  | true 
		true             | false             | [validateResponses: false, validateRequests: true ]  | true                  | false
		false            | false             | [validateResponses: false, validateRequests: false]  | false                 | false
		true             | true              | [:                                                ]  | true                  | true 
		false            | false             | [validateResponses: true , validateRequests: false]  | false                 | true 
		false            | false             | [validateResponses: false, validateRequests: true ]  | true                  | false
		false            | false             | [validate: true                                   ]  | true                  | true 
	}
	
	def "should create mock"() {
		expect:
		templateConfig(mockClass: mockClass, parameters: toConfigObject(mock: mock)).shouldCreateMock == should
		
		where:
		mockClass | mock  | should
		null      | false | false
		String    | false | false
		null      | true  | true
		String    | true  | true  
	}
	
}