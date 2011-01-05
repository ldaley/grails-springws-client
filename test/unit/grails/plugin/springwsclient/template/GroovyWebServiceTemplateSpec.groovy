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

import grails.plugin.springwsclient.mock.http.HttpSoapWebServiceMock

import spock.lang.*
import grails.plugin.spock.*

import grails.plugin.springwsclient.marshalling.*

class GroovyWebServiceTemplateSpec extends UnitSpec {

	class TestingServiceMock extends HttpSoapWebServiceMock {
		Closure bodyCallback
		Closure headerCallback
		
		void service(req, res) {
			if (bodyCallback) {
				if (bodyCallback.maximumNumberOfParameters > 1) {
					bodyCallback(req, res)
					res.result(true)
				} else {
					bodyCallback(req)
				}
			} else {
				res.result(true)
			}
		}
		
		void serviceHeader(header) {
			if (headerCallback) {
				headerCallback(header)
			}
		}
	}

	def template
	def mock
	
	def setup() {
		mock = new TestingServiceMock()
		mock.start()
		
		template = new GroovyWebServiceTemplate()
		template.destinationProvider = mock
		template.marshaller = new MarkupBuilderMarshaller()
		template.unmarshaller = new XmlSlurperUnmarshaller()
	}
	
	def "header generation"() {
		given:
		def usernameValue
		
		mock.headerCallback = {
			usernameValue = it.username.text()
		}
		
		def header = template.header {
			username("me")
		}
		
		when:
		template.call(header) { abc("123") }
		
		then:
		usernameValue == "me"
	}
	
	
	def cleanup() {
		mock.stop()
	}
	
}