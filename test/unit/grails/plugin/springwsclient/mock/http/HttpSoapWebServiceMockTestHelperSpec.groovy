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

package grails.plugin.springwsclient.mock.http

import spock.lang.*
import grails.plugin.spock.*

class HttpSoapWebServiceMockTestHelperSpec extends UnitSpec {

	class AddingServiceMock extends HttpSoapWebServiceMock {
		void service(req, res) {
			res.result(req.text().toInteger() + value)
		}
	}

 	def value = 1 
	def mock = new AddingServiceMock()
	def helper = new HttpSoapWebServiceMockTestHelper()
	
	def setup() {
		mock.start()
	} 

	def "simple"() {
		expect:
		helper.request(mock) { number("1") }.text() == "2"
	}

	def "callbacks"() {
		given:
		def requestCallbackCalled = false
		def responseCallbackCalled = false
		
		and:
		helper.requestCallback = { requestCallbackCalled = true }
		helper.responseCallback = { responseCallbackCalled = true }

		when:
		helper.request(mock) { number("1") }
		
		then:
		requestCallbackCalled
		responseCallbackCalled
	}
	
	def cleanup() {
		mock.stop()
	}
	
}