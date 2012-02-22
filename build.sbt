name := "spring-integration-scala"

version := "1.0.0.BUILD-SNAPSHOT"

organization := "org.springframework"

scalaVersion := "2.9.1"

resolvers += "Spring Milestone Repository" at "http://repo.springsource.org/libs-milestone"

resolvers += "Spring Snapshot Repository" at "https://repo.springsource.org/libs-snapshot"

resolvers += "Spring Release Repository" at "https://repo.springsource.org/libs-release"

libraryDependencies += "log4j" % "log4j" % "1.2.16"

libraryDependencies += "org.springframework.integration" % "spring-integration-core" % "2.1.1.BUILD-SNAPSHOT"

libraryDependencies += "com.novocode" % "junit-interface" % "0.7" % "test->default"