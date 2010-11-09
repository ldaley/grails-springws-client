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

package grails.plugin.springwsclient.marshalling
import javax.xml.transform.Result
import javax.xml.transform.TransformerFactory

import org.springframework.xml.transform.StringSource

import groovy.xml.MarkupBuilder

class MarkupBuilderMarshaller implements BuildingMarshaller {

	private transformerFactory = TransformerFactory.newInstance()
	
	protected Writer createWriter() {
		new StringWriter()
	}

	protected createBuilder(Writer writer) {
		new MarkupBuilder(writer)
	}
	
	void withBuilder(Result result, Closure block) {
		doMarshal(result) { writer ->
			block.call(createBuilder(writer))
		}
	}
	
	void marshal(graph, Result result) {
		doMarshal(result) { writer ->
			graph.delegate = createBuilder(writer)
			graph()
		}
	}
	
	protected doMarshal(Result result, Closure block) {
		def writer = createWriter()
		
		block(writer)
		
		def source = new StringSource(writer.toString())
		def transformer = transformerFactory.newTransformer()
		transformer.transform(source, result)
	}
	
	boolean supports(Class clazz) {
		Closure.isAssignableFrom(clazz)
	}

}