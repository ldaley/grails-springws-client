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

import org.springframework.ws.soap.SoapVersion
import org.springframework.ws.soap.SoapMessageFactory
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory

import org.springframework.oxm.Marshaller
import org.springframework.oxm.Unmarshaller
import org.springframework.xml.transform.StringSource
import org.springframework.xml.transform.StringResult

import grails.plugin.springwsclient.marshalling.BuildingMarshaller
import grails.plugin.springwsclient.marshalling.MarkupBuilderMarshaller
import grails.plugin.springwsclient.marshalling.XmlSlurperUnmarshaller
import groovy.xml.XmlUtil

import groovy.util.slurpersupport.GPathResult
import grails.plugin.springwsclient.util.GPathResultDumper

/**
 * Designed to be used in testing web service mock implementations.
 * 
 * It is designed to be used via composition, not via inheritance.
 */
class HttpSoapWebServiceMockTestHelper {

	protected SoapVersion soapVersion
	protected SoapMessageFactory messageFactory
	protected Marshaller marshaller
	protected Unmarshaller unmarshaller
	
	Closure requestCallback
	Closure responseCallback
	
	HttpSoapWebServiceMockTestHelper(SoapVersion soapVersion = null, SoapMessageFactory messageFactory = null, Marshaller marshaller = null, Unmarshaller unmarshaller = null) {
		this.soapVersion = soapVersion ?: getDefaultSoapVersion()
		this.messageFactory = messageFactory ?: createDefaultMessageFactory()
		this.marshaller = marshaller ?: createDefaultMarshaller()
		this.unmarshaller = unmarshaller ?: createDefaultUnmarshaller()
	}

	protected SoapVersion getDefaultSoapVersion() {
		SoapVersion.SOAP_11
	}
	
	protected SoapMessageFactory createDefaultMessageFactory() {
		def messageFactory = new SaajSoapMessageFactory()
		messageFactory.soapVersion = this.soapVersion
		messageFactory.afterPropertiesSet()
		messageFactory
	}
	
	protected Marshaller createDefaultMarshaller() {
		new MarkupBuilderMarshaller()
	}

	protected Unmarshaller createDefaultUnmarshaller() {
		new XmlSlurperUnmarshaller()
	}
	
	protected HttpURLConnection openConnection(HttpSoapWebServiceMock mock) {
		mock.destination.toURL().openConnection()
	}
	
	def request(HttpSoapWebServiceMock mock, requestContent) {
		// Assuming the unmarshaller is gpath based here
		requestEnvelope(mock, requestContent).'Body'.children()[0]
	}
	
	def requestEnvelope(HttpSoapWebServiceMock mock, requestContent) {
		def message = messageFactory.createWebServiceMessage()
		marshaller.marshal(requestContent, message.payloadResult)
		
		def out = new ByteArrayOutputStream()
		message.writeTo(out)
		def requestText = out.toString()
		
		def responseText = doSend(mock, requestText)
		unmarshaller.unmarshal(new StringSource(responseText))
	}
	
	protected String doSend(HttpSoapWebServiceMock mock, String content) {
		openConnection(mock).with {
			setRequestProperty("Content-Type", soapVersion.contentType)
			setRequestProperty("accept", soapVersion.contentType)
			
			instanceFollowRedirects = true
			doOutput = true
		
			requestCallback?.call(delegate)
			outputStream << content
			responseCallback?.call(delegate)
			
			inputStream.text
		}
	}
	
	void assertIsRunning(HttpSoapWebServiceMock mock) {
		if (!mock.running) {
			throw new IllegalStateException("the web service mock '$mock' is not running")
		}
	}
	
	void dump(GPathResult xml, out = System.out) {
		GPathResultDumper.dump(xml, out)
	}
}