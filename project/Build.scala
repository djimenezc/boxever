import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "currencyMonitor"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "com.netflix.astyanax" % "astyanax" % "1.56.26",
    "org.springframework"  %  "spring-test" % "3.1.2.RELEASE" % "test", 
    "org.springframework"  %  "spring-expression" % "3.1.2.RELEASE" % "test", 
    "org.springframework"  %  "spring-asm" % "3.1.2.RELEASE" % "test",
    "org.jdom"  %  "jdom" % "1.1.3",
    "jaxen"  %  "jaxen" % "1.1.4"
    
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )
  
}
