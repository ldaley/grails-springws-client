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

package grails.plugin.springwsclient.template.message

import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.WebServiceMessageCallback
import org.springframework.ws.soap.client.core.SoapActionCallback

class SoapMessage {
	String uri
	Object body
	List<WebServiceMessageCallback> callbacks = []
	
	void build(Closure definition) {
		definition.delegate = new SoapMessageBuilder()
		definition()
	}
	
	WebServiceMessageCallback getCallback() {
		callbacks ? new CompositeWebServiceMessageCallback(*callbacks) : new NoopWebServiceMessageCallback()
	}
	
	class SoapMessageBuilder {

		void uri(String uri) {
			SoapMessage.this.uri = uri
		}
		
		void callback(WebServiceMessageCallback callback) {
			SoapMessage.this.callbacks << callback
		}

		void action(String action) {
			callback(new SoapActionCallback(actionName))
		}
		
		void header(Closure definition) {
			callback(new HeaderBuildingCallback(definition))
		}
		
		void body(Object definition) {
			SoapMessage.this.body = definition
		}
		
	}

}