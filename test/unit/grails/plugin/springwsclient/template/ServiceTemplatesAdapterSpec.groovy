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

package grails.plugin.springwsclient.template

class ServiceTemplatesAdapterSpec extends TemplateSpecUtils {
	
	def "retrieve definitions"() {
		when:
		def wsclients = { 1 }
		
		then:
		ServiceTemplatesAdapter.getDefinitions(createServiceClassWithWsClients(wsclients)) == wsclients
	}
	
	def "retrieve invalid definitions"() {
		when:
		ServiceTemplatesAdapter.getDefinitions(createServiceClassWithWsClients(123)) == wsclients
		
		then:
		thrown(IllegalStateException)
	}
	
	def "no definitions"() {
		expect:
		ServiceTemplatesAdapter.getDefinitions(createServiceClassWithWsClients(null)) == null
	}
	
	def "creating instance"() {
		when:
		def adapter = createAdapter {
			c1()
			c2()
		}
		
		then:
		adapter.templateConfigs*.name == ["c1", "c2"]
	}

	def "creating instance with params"() {
		given:
		def wsclients = {
			c1()
		}
		
		when:
		def adapter = createAdapter(wsclients) {
			it.clients.c1.validate = true
		}

		then:
		adapter.templateConfigs*.validateRequests == [true]
	}
	
}