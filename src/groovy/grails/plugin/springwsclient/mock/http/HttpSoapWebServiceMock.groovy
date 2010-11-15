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

import org.springframework.oxm.Marshaller
import org.springframework.oxm.Unmarshaller
import org.springframework.xml.transform.StringSource
import org.springframework.xml.transform.StringResult
import org.springframework.ws.soap.SoapVersion
import org.springframework.ws.soap.SoapMessageFactory
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory
import org.springframework.ws.context.MessageContext
import org.springframework.ws.context.DefaultMessageContext
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.server.endpoint.PayloadEndpoint
import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter

import grails.plugin.springwsclient.marshalling.BuildingMarshaller
import grails.plugin.springwsclient.marshalling.MarkupBuilderMarshaller
import grails.plugin.springwsclient.marshalling.XmlSlurperUnmarshaller

import groovy.xml.XmlUtil

/**
 * A base class for creating web service mocks for SOAP over HTTP web services.
 */
abstract class HttpSoapWebServiceMock extends HttpWebServiceMock implements PayloadEndpoint {
	
	private adapter = new PayloadEndpointAdapter()
	
	protected Marshaller marshaller
	protected Unmarshaller unmarshaller
	protected TransformerFactory transformerFactory
	protected SoapMessageFactory messageFactory

	/**
	 * Calls various create*() methods, allowing subclass to customise the behaviour.
	 */
	HttpSoapWebServiceMock() {
		marshaller = createMarshaller()
		unmarshaller = createUnmarshaller()
		transformerFactory = createTransformerFactory()
		messageFactory = createMessageFactory()
	}
	
	/**
	 * Called with the unmarshalled request, and the builder from the building marshaller if the configured
	 * marshaller is a building marshaller.
	 * 
	 * Subclasses should override this or service(request) to implement the mocking logic
	 */
	void service(request, response) {
		throw new UnsupportedOperationException("this implementation does not support this method")
	}

	/**
	 * Called with the unmarshalled request. The return value from this method will be marshalled
	 * using the configured marshaller.
	 * 
	 * Subclasses should override this or service(request, response) to implement the mocking logic
	 */	
	def service(request) {
		throw new UnsupportedOperationException("this implementation does not support this method")
	}
	
	protected willStart() {
		addServlet(createServlet())
	}
	
	protected TransformerFactory createTransformerFactory() {
		TransformerFactory.newInstance()
	}
	
	protected Marshaller createMarshaller() {
		new MarkupBuilderMarshaller()
	}

	protected Unmarshaller createUnmarshaller() {
		new XmlSlurperUnmarshaller()
	}

	/**
	 * Hook for changing the servlet impl.
	 */
	protected HttpServlet createServlet() {
		new HttpSoapWebServiceMockServlet()
	}
	
	/**
	 * Used by the default createMessageFactory() impl to control the soap version, and used to 
	 * control the content-type headers.
	 */
	protected SoapVersion getSoapVersion() {
		SoapVersion.SOAP_11
	}

	protected SoapMessageFactory createMessageFactory() {
		def messageFactory = new SaajSoapMessageFactory()
		messageFactory.soapVersion = getSoapVersion()
		messageFactory.afterPropertiesSet()
		messageFactory
	}
	
	protected MessageContext createMessageContext(WebServiceMessage message, SoapMessageFactory messageFactory) {
		new DefaultMessageContext(message, messageFactory)
	}
	
	/**
	 * Utility to aid in debugging.
	 */
	protected dump(xml, out = System.out) {
		XmlUtil.serialize(xml, out)
	}
	
	/**
	 * Subclass hook to allow processing of the request at the HTTP level.
	 * 
	 * This can be used to simulate different HTTP level error conditions or situations.
	 * 
	 * @return true if the processing of the request should proceed after this, false to halt processing.
	 */
	protected boolean processRequest(HttpServletRequest req, HttpServletResponse res) {
		true
	}
	
	/**
	 * Called when the service impl has been called successfully and has produced a response.
	 */
	protected handleResponse(MessageContext messageContext, HttpServletRequest req, HttpServletResponse res) {
		res.contentType = soapVersion.contentType
		res.status = 200
		messageContext.response.writeTo(res.outputStream)
	}

	/**
	 * Called when the service impl has been called successfully but has produced no response.
	 */	
	protected handleNoResponse(MessageContext messageContext, HttpServletRequest req, HttpServletResponse res) {
		res.contentType = "text/html"
		res.status = 200
	}

	/**
	 * Called when an exception occurred while processing the request.
	 */
	protected handleError(MessageContext messageContext, Throwable e, HttpServletRequest req, HttpServletResponse res) {
		e.printStackTrace()
		res.contentType = "text/plain"
		res.status = 500
		res.outputStream << e.toString()
		e.printStackTrace()
	}
	
	/**
	 * 
	 */
	protected receive(MessageContext messageContext) {
		adapter.invoke(messageContext, this)
	}

	/**
	 * PayloadEndpoint method.
	 */
	Source invoke(Source request) {
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

	private class HttpSoapWebServiceMockServlet extends HttpServlet {
		protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			if (processRequest(req, res)) {
				def message = messageFactory.createWebServiceMessage(req.inputStream)
				def messageContext = createMessageContext(message, messageFactory)
				
				try {
					receive(messageContext)
					
					if (messageContext.hasResponse()) {
						handleResponse(messageContext, req, res)
					} else {
						handleNoResponse(messageContext, req, res)
					}
				} catch (Throwable e) {
					handleError(messageContext, e, req, res)
				}
			}
		}
	}
	
}