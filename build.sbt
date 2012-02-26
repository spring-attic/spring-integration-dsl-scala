name := "spring-integration-scala"

version := "1.0.0.BUILD-SNAPSHOT"

organization := "org.springframework"

scalaVersion := "2.9.1"

resolvers += "Spring Milestone Repository" at "http://repo.springsource.org/libs-milestone"

resolvers += "Spring Snapshot Repository" at "https://repo.springsource.org/libs-snapshot"

resolvers += "Spring Release Repository" at "https://repo.springsource.org/libs-release"

libraryDependencies += "org.springframework.integration" % "spring-integration-core" % "2.1.0.RELEASE"

libraryDependencies += "org.springframework.integration" % "spring-integration-http" % "2.1.0.RELEASE"

libraryDependencies += "org.springframework.integration" % "spring-integration-jms" % "2.1.0.RELEASE"

libraryDependencies += "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "1.1"

libraryDependencies += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.2"

libraryDependencies += "com.novocode" % "junit-interface" % "0.7" % "test->default"

libraryDependencies += "log4j" % "log4j" % "1.2.16" % "test->default"

libraryDependencies += "org.apache.activemq" % "activemq-core" % "5.3.0" % "test->default"
