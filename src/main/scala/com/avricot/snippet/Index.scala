package com.avricot.snippet

import scala.xml.{ NodeSeq, Text }
import net.liftweb._
import common._
import http.{ LiftRules, S }
import sitemap.SiteMap
import util.Helpers._
import com.avricot.lib.AppHelpers
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.js.JsCmds.{ Alert, Script, SetHtml }
import net.liftweb.http.SHtml
import net.liftweb.http.SessionVar
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn
import net.liftweb.http.js.JE._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import scala.util.matching.Regex
import ch.qos.logback.core.joran.spi.Pattern

object Index {
  /**
   * Init default index page js.
   */
  def initJs(in: NodeSeq): NodeSeq =
      Script(OnLoad(JE.JsRaw("new ALOG.page.Index()").cmd)
   )
}

/*
 * Create a new application.
 */
object AddNewApplication  extends BaseScreen {
  val name = field("Application name", "")
  val ip = field("Server Ip", "")
  val ipRegex = """(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)""".r.pattern ;
  override def finish() {
    
  }
  override def calcAjaxOnDone : JsCmd = {
    if(name.length()<2 || name.length()>20) {
    	BootstrapAlerts.error("Application name ("+name+") is invalid. Min is 2 chars, Max is 20 chars.")
    } else if(!ipRegex.matcher(ip).matches()) {
    	BootstrapAlerts.error("Bad Ip")
    } else {
    	BootstrapAlerts.notice("ok"+ip.is)  //Alert("Form submitted!")
    }
  }
}