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

class MockHttpService implements InitializingBean, DisposableBean {

	final protected Map<String,Map<String,Servlet>> contexts = [:]

	protected server 
	boolean started
	
	void afterPropertiesSet() {
		start()
	}
	
	void start() {
		if (!started) {
			server = new Server(0)
			contexts.each { path, servlets ->
				def context = new Context(server, path)
				servlets.each { pathSpec, servlet ->
					context.addServlet(new ServletHolder(servlet), pathSpec)
				}
			}
			
			server.start()
			started = true
		}
	}
	
	void destroy() {
		stop()
	}
	
	void stop() {
		if (started) {
			server.stop()
			started = false
		}
	}
	
	def getPort() {
		server?.connectors[0].localPort
	}
	
	void addServlet(servlet, context = "/", pathSpec = '/*') {
		contexts[context] = [(pathSpec): servlet]
	}
	
	void putAt(String context, Servlet servlet) {
		addServlet(servlet, context)
	}
	
	void putAt(List contextAndPathSpec, Servlet servlet) {
		def (context, pathSpec) = contextAndPathSpec
		addServlet(servlet, context, pathSpec)
	}
	
	def leftShift(servlet) {
		addServlet(servlet)
	}
}