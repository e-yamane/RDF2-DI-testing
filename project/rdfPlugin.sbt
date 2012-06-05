
resolvers += Resolver.url("Rough Diamond Framework Repository", url("http://133.242.22.208/repos/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "org.jacoco" % "org.jacoco.core" % "0.5.6.201201232323" artifacts(Artifact("org.jacoco.core", "jar", "jar")),
  "org.jacoco" % "org.jacoco.report" % "0.5.6.201201232323" artifacts(Artifact("org.jacoco.report", "jar", "jar")))

addSbtPlugin("de.johoop" % "jacoco4sbt" % "1.2.1")

libraryDependencies += "eu.henkelmann" % "junit_xml_listener" % "0.3"
