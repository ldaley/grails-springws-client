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

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.xml.transform.TransformerFactory

import javax.xml.transform.Source
import org.springframework.xml.transform.StringSource
import org.springframework.xml.transform.StringResult

import org.springframework.ws.soap.SoapVersion

import org.springframework.ws.soap.saaj.SaajSoapMessageFactory

import org.springframework.ws.context.MessageContext
import org.springframework.ws.context.DefaultMessageContext

import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter
import org.springframework.ws.server.endpoint.PayloadEndpoint

import grails.plugin.springwsclient.marshalling.BuildingMarshaller
import grails.plugin.springwsclient.marshalling.MarkupBuilderMarshaller
import grails.plugin.springwsclient.marshalling.XmlSlurperUnmarshaller
import grails.plugin.springwsclient.marshalling.GPathResultDumper

import org.springframework.oxm.Marshaller
import org.springframework.oxm.Unmarshaller

import org.springframework.ws.client.support.destination.DestinationProvider

abstract class MockHttpSoapService extends MockHttpService implements PayloadEndpoint, DestinationProvider {
	
	private servlet
	private transformerFactory = TransformerFactory.newInstance()
	private adapter = new PayloadEndpointAdapter()

	MockHttpSoapService() {
		def defaultServlet = createDefaultServlet()
		if (defaultServlet) {
			addServlet(defaultServlet)
		}
	}

	Marshaller getMarshaller() {
		new MarkupBuilderMarshaller()
	}

	Unmarshaller getUnmarshaller() {
		new XmlSlurperUnmarshaller()
	}

	protected createDefaultServlet() {
		new MockHttpSoapServiceServlet(this, createMessageFactory())
	}
	
	SoapVersion getSoapVersion() {
		SoapVersion.SOAP_11
	}
	
	protected dump(xml, out = System.out) {
		GPathResultDumper.dump(xml, out)
	}
	
	protected createMessageFactory() {
		def messageFactory = new SaajSoapMessageFactory()
		messageFactory.soapVersion = getSoapVersion()
		messageFactory.afterPropertiesSet()
		messageFactory
	}

	URI getDestination() {
		new URI("http://localhost:${getPort()}")
	}
	
	void receive(MessageContext messageContext) {
		adapter.invoke(messageContext, this)
	}

	Source invoke(Source request) {
		def unmarshaller = getUnmarshaller()
		def marshaller = getMarshaller()
		
		def requestPayload = unmarshaller.unmarshal(request)
		def responseResult = new StringResult()
		
		if (marshaller instanceof BuildingMarshaller) {
			marshaller.withBuilder(responseResult) {
				service(requestPayload, it)
			}
		} else {
			marshaller.marshal(service(requestPayload, null), responseResult)
		}
		
		new StringSource(responseResult.toString())
	}

	static protected class MockHttpSoapServiceServlet extends HttpServlet {

		private server
		private messageFactory

		MockHttpSoapServiceServlet(server, messageFactory) {
			this.server = server
			this.messageFactory = messageFactory
		}

		protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			try {
				def message = messageFactory.createWebServiceMessage(req.inputStream)
				def messageContext = new DefaultMessageContext(message, messageFactory)

				server.receive(messageContext)

				if (messageContext.hasResponse()) {
					res.contentType = server.soapVersion.contentType
					res.status = 200
					messageContext.response.writeTo(res.outputStream)
				} else {
					res.contentType = "text/html"
					res.status = 200
				}
			} catch (Throwable e) {
				e.printStackTrace()
				res.contentType = "text/plain"
				res.status = 500
				res.outputStream << e.toString()
				e.printStackTrace()
			}
		}
	}
	
	abstract service(request, response)
	
}