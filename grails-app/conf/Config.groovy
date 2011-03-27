// configuration for plugin testing - will not be included in the plugin zip
 
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'

	info   'grails.plugin.springwsclient'
}

springwsclient {
	clients {
		doubling {
			mock = true
		}
	}
}

grails {
    doc {
        title = "Grails Spring WS Client Plugin"
        subtitle = "Spring WS Client for Grails"
        authors = "Luke Daily"
        copyright = "Copies of this document may be made for your own use and for distribution to others, provided that you do not charge any fee for such copies and further provided that each copy contains this Copyright Notice, whether distributed in print or electronically."
        footer = "Developed by the <a href='http://alkemist.github.com'>Luke Daily</a>"
        license = "Apache License 2.0"
        api {
            org.springframework.ws.client="http://static.springsource.org/spring-ws/site/apidocs"
        }
    }

}