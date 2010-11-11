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

/**
 * A template config factory that creates templates by cloning a given default,
 * and setting the parameters via a given parameter source.
 */
class DefaultCloningTemplateConfigFactory implements TemplateConfigFactory {

	final TemplateConfig defaultConfig
	final ParameterSource parameterSource
	
	DefaultCloningTemplateConfigFactory(TemplateConfig defaultConfig, ParameterSource parameterSource) {
		this.defaultConfig = defaultConfig
		this.parameterSource = parameterSource
	}
	
	TemplateConfig createForName(String name) {
		def cloned = defaultConfig.clone()
		cloned.name = name
		cloned.parameters = parameterSource.getParametersForTemplate(name)
		cloned
	}

}