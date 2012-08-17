package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.java.sql.{ Connection, DriverManager }
import com.avricot.model.User
import shiro.Shiro
import org.apache.shiro.config.IniSecurityManagerFactory

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {

  def boot {
    Shiro.init(new IniSecurityManagerFactory("classpath:shiro.ini"))
    logger.info("Run Mode: " + Props.mode.toString)
    //init mongodb
    // MongoConfig.init()

    // init mongoauth
    // MongoAuth.authUserMeta.default.set(User)
    // MongoAuth.indexUrl.default.set("/")

    // For S.loggedIn_? and TestCond.loggedIn/Out builtin snippet
    // LiftRules.loggedInTest = Full(() => User.isLoggedIn)

    // checks for ExtSession cookie
    // LiftRules.earlyInStateful.append(User.testForExtSession)

    LiftRules.docType.default.set((r: Req) => Full(DocType.html5))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    // where to search snippet
    LiftRules.addToPackages("com.avricot")

    // Build SiteMap
    //    def sitemap() = SiteMap(
    //      // Menu with special Link
    //      Menu(Loc("Static", Link(List("static"), true, "/static/index"),
    //        "Static Content")))
    //
    //    LiftRules.setSiteMap(SiteMap(List(
    //      Locs.buildLogoutMenu,
    //      Locs.buildLoginTokenMenu): _*))
    // Build SiteMap
    LiftRules.setSiteMap(Site.siteMap)

    // Error handler
    ErrorHandler.init

    // 404 handler
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) =>
        NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    //    LiftRules.snippetDispatch.append {
    //    	case "MainMenu" => MainMenu
    //	}

  }

}
