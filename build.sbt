import AssemblyKeys._

assemblySettings

proguardSettings

name := "CFClient-Scala"

version := "1.1"

scalaVersion := "2.11.0-M8"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "org.scalatest"           % "scalatest_2.11.0-M8" % "2.1.RC1" % "test"

libraryDependencies += "junit"                   % "junit"               % "4.11"    % "test"

exportJars := true

ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings")

ProguardKeys.options in Proguard += ProguardOptions.keepMain("cfclient.Main")

ProguardKeys.inputFilter in Proguard := { file =>
  file.name match {
    case "scala-library-2.11.0-M8.jar"          => Some("!META-INF/**")
    case "regextractor-core_2.11.0-M8-0.1.jar"  => Some("!META-INF/**")
    case "regextractor-util_2.11.0-M8-0.1.jar"  => Some("!META-INF/**")
    case "rxjava-core-0.16.2-SNAPSHOT.jar"      => Some("!META-INF/**")
    case "rxjava-scala-0.16.2-SNAPSHOT.jar"     => Some("!META-INF/**")
    case _ => None
  }
}
