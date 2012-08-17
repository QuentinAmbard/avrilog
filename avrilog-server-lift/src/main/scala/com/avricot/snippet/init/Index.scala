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

object InitApp {
  def initUser(in: NodeSeq): NodeSeq =
    Script(OnLoad(JE.JsRaw("new ALOG.page.Index()").cmd))
}
