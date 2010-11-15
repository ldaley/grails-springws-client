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

package grails.plugin.springwsclient.util

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil

/**
 * Utility to pretty print part of an XmlSlurper tree.
 * 
 * Can be useful when debugging response handling code.
 */
class GPathResultDumper {

	static dump(GPathResult node, OutputStream out = System.out) {
		dump(node, new OutputStreamWriter(out))
		out
	}
	
	static dump(GPathResult node, Writer writer) {
		def printer = new XmlNodePrinter(new PrintWriter(writer))
		printer.preserveWhitespace = true
		printer.print(new XmlParser().parseText(XmlUtil.serialize(node)))
		writer
	}

	static dumpAsString(GPathResult node) {
		dump(node, new StringWriter()).toString()
	}
}