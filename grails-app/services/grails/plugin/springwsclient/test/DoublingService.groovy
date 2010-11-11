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
package grails.plugin.springwsclient.test

/**
 * Simple test service used in the smoke test.
 */
class DoublingService {

	static transactional = false
	
	def wsclients = {
		doubling { 
			mock MockDoublingHttpSoapService
			log true
			schema "classpath:schema/number.xsd"
			validate true
		}
	}

	def doubleIt(num) {
		doublingWsClient.marshalSendAndReceive { number(xmlns: "n", num.toString()) }.text().toInteger()
	}
}