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

import org.springframework.ws.client.core.WebServiceTemplate

import org.springframework.ws.client.core.WebServiceMessageCallback
import org.springframework.ws.soap.client.core.SoapActionCallback
import org.springframework.ws.WebServiceMessage

import grails.plugin.springwsclient.marshalling.MarkupBuilderMarshaller

class GroovyWebServiceTemplate extends WebServiceTemplate {

	static private class ClosureWebServiceMessageCallback {
		final private callback

		ClosureWebServiceMessageCallback(Closure callback) {
			this.callback = callback
		}
		
		void doWithMessage(WebServiceMessage message) {
			callback(message)
		}
	}
	
	WebServiceMessageCallback callback(Closure callback) {
		new ClosureWebServiceMessageCallback(callback)
	}

	SoapActionCallback actionCallback(String actionName) {
		new SoapActionCallback(actionName)
	}
	
	def call(Object payload) {
		marshalSendAndReceive(payload)
	}
	
	def call(Object payload, String action) {
		marshalSendAndReceive(payload, actionCallback(action))
	}
	
	def call(Object payload, Closure callbackImpl) {
		marshalSendAndReceive(payload, callback(callbackImpl))
	}
	
	def call(Object payload, String action, Closure callbackImpl) {
		def compositeCallback = {
			actionCallback(it)
			callbackImpl(it)
		}
		marshalSendAndReceive(payload, callback(compositeCallback))
	}
	
	def call(String action, Closure payload) {
		marshalSendAndReceive(payload, actionCallback(action))
	}
	
	def call(WebServiceMessageCallback callback, payload) {
		marshalSendAndReceive(payload, callback)
	}
	
	def header(Closure block) {
		new HeaderBuildingCallback(block)
	}
	
	static private class HeaderBuildingCallback implements WebServiceMessageCallback {
		private Closure definition
		
		HeaderBuildingCallback(Closure definition) {
			this.definition = definition
		}
		
		void doWithMessage(WebServiceMessage message) {
			new MarkupBuilderMarshaller().withBuilder(message.soapHeader.result) {
				definition.delegate = it
				definition.call()
			}
		}
	}
}