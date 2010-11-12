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

import java.io.IOException

import javax.servlet.Servlet
import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.DisposableBean

import grails.plugin.springwsclient.mock.WebServiceMock

/**
 * A lightweight http servlet server that always uses an ephemeral port.
 * 
 * This implementation does not support adding servlets after the server has been started.
 * Subclasses should override the willStart() method and register servlets there.
 */
abstract class HttpWebServiceMock implements WebServiceMock, InitializingBean, DisposableBean {

	final private Map<String,Map<String,Servlet>> contexts = [:]

	protected Server server 
	private boolean running
	
	boolean isRunning() {
		this.running
	}
	
	void afterPropertiesSet() {
		start()
	}

	void destroy() {
		stop()
	}
	
	protected getDestinationURIPath() {
		""
	}
	
	URI getDestination() {
		if (running) {
			new URI("http://localhost:${getPort()}" + getDestinationURIPath())
		} else {
			null
		}
	}
	
	void start() {
		if (!running) {
			willStart()
			server = new Server(0)
			contexts.each { path, servlets ->
				def context = new Context(server, path)
				servlets.each { pathSpec, servlet ->
					context.addServlet(new ServletHolder(servlet), pathSpec)
				}
			}
			
			server.start()
			running = true
		}
	}
	
	
	void stop() {
		if (running) {
			server.stop()
			server = null
			started = false
		}
	}
	
	/**
	 * Will return null if the server 
	 */
	def getPort() {
		server?.connectors[0].localPort
	}
	
	/**
	 * Subclass hook to register servlets before the server starts
	 */
	protected willStart() {
		
	}
	
	/**
	 * The mechanism for registering servlets
	 */
	protected void addServlet(servlet, context = "/", pathSpec = '/*') {
		contexts[context] = [(pathSpec): servlet]
	}
	
}