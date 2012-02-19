name := "spring-integration-scala"

version := "0.0.1.M1"

organization := "org.springframework"

scalaVersion := "2.9.1"

resolvers += "Spring Framework Maven Release Repository" at "http://maven.springframework.org/release"

libraryDependencies += "log4j" % "log4j" % "1.2.16"

libraryDependencies += "org.springframework.integration" % "spring-integration-core" % "2.1.0.RELEASE"

libraryDependencies += "com.novocode" % "junit-interface" % "0.7" % "test->default"