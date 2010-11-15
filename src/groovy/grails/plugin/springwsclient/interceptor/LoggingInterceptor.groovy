/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugin.springwsclient.interceptor

import org.springframework.xml.transform.TransformerObjectSupport
import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.context.MessageContext
import groovy.xml.XmlUtil
import groovy.xml.StreamingMarkupBuilder
import org.slf4j.LoggerFactory

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A client interceptor that can be used to log the entire web service message selectively,
 * for requests, responses and/or faults.
 */
class LoggingInterceptor implements ClientInterceptor {

	protected final log
	
	final boolean logRequest
	final boolean logResponse
	final boolean logFault
	
	protected final DocumentBuilder documentBuilder
	
	LoggingInterceptor(String logName, boolean logRequest, boolean logResponse, boolean logFault, DocumentBuilder documentBuilder = null) {
		this.log = LoggerFactory.getLogger(logName)
		
		this.logRequest = logRequest
		this.logResponse = logResponse
		this.logFault = logFault
		
		this.documentBuilder = documentBuilder ?: createDocumentBuilder()
	}

	protected DocumentBuilder createDocumentBuilder() {
		DocumentBuilderFactory.newInstance().newDocumentBuilder()
	}
	
	boolean handleFault(MessageContext messageContext) {
		if (logFault) {
			def response = messageContext.response
			if (response) {
				doLog("fault --\n", response)
			}
		}
		true
	}

	boolean handleRequest(MessageContext messageContext) {
		if (logRequest) {
			def request = messageContext.request
			if (request) {
				doLog("request --\n", request)
			}
		}
		true
	}
	
	boolean handleResponse(MessageContext messageContext) {
		if (logResponse) {
			def response = messageContext.response
			if (response) {
				doLog("response --\n", response)
			}
		}
		true
	}

	protected doLog(String preamble, WebServiceMessage message) {
		if (log.infoEnabled) {
			log.info(preamble + messageAsString(message))
		}
	}

	protected messageAsString(WebServiceMessage message) {
		def baos = new ByteArrayOutputStream()
		message.writeTo(baos)
		
		def stringWriter = new StringWriter()
		def node = new XmlParser().parseText(new String(baos.toByteArray()))
		def printer = new XmlNodePrinter(new PrintWriter(stringWriter))
		printer.preserveWhitespace = true
		printer.print(node)

		stringWriter.toString()
	}
}