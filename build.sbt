import AssemblyKeys._

assemblySettings

name := "CFClient-Scala"

version := "1.1"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.netflix.rxjava"      % "rxjava-scala"        % "0.19.6"

libraryDependencies += "org.scalatest"           % "scalatest_2.11"      % "2.2.1" % "test"

libraryDependencies += "junit"                   % "junit"               % "4.11"  % "test"

exportJars := true
