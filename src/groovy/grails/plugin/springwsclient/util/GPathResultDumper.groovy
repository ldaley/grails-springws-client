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

import groovy.xml.MarkupBuilder

/**
 * Utility to print part of an XmlSlurper tree.
 * 
 * Can be useful when debugging response handling code.
 */
class GPathResultDumper {

	static dump(xml, out = System.out) {
		def builder = new MarkupBuilder(new OutputStreamWriter(out))
		
		def traverse
		traverse = { node ->
			def children = {
				node.children().each {
					traverse(it)
				}
			}
				
			if (node.text() && node.attributes()) {
				builder."${node.name()}"(node.attributes(), node.text(), children) 
			} else if (node.text()) {
				builder."${node.name()}"(node.text(), children) 
			} else if (node.attributes()) {
				builder."${node.name()}"(node.attributes(), children) 
			} else {
				builder."${node.name()}"(children) 
			}
		}
		
		traverse(xml)
	}

}