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

import spock.lang.*
import grails.plugin.spock.*

class GPathResultDumperSpec extends UnitSpec {

	def gpathResult
	
	def "no namespace"() {
		when:
		input = "<a><b>1</b></a>"
		
		then:
		output == "<a>\n  <b>1</b>\n</a>\n"
	}

	// The tag0 bits are added by StreamingMarkupBuilder, which is used by XmlUtils.serialize()
	// I can't find a way to avoid it, but it is harmless.
	def "with namespace"() {
		when:
		input = "<a xmlns='1'><b>1</b></a>"
		
		then:
		output == '<tag0:a xmlns:tag0="1">\n  <tag0:b>1</tag0:b>\n</tag0:a>\n'
	}
	
	void setInput(String xml) {
		gpathResult = new XmlSlurper().parseText(xml)
	}

	String getOutput() {
		GPathResultDumper.dumpAsString(gpathResult)
	}
}