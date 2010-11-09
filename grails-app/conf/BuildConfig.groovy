grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits("global")
	log "warn"

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile 'org.springframework.ws:spring-ws-core:1.5.9'
		compile "org.mortbay.jetty:jetty:6.1.22" // used for mock http soap implementations
	}
}
