package com.avricot.snippet

import bootstrap.liftweb.Site
import com.avricot.lib.{AppHelpers}
import com.avricot.model.{User, LoginCredentials}

import scala.xml._

import net.liftweb._
import common._
import http.{DispatchSnippet, S, SHtml, StatefulSnippet}
import util._
import Helpers._

sealed trait UserSnippet extends DispatchSnippet with AppHelpers with Loggable {

  def dispatch = {
    case "header" => header
    case "name" => name
    case "username" => username
    case "title" => title
  }

  protected def user: Box[User]

  protected def serve(snip: User => NodeSeq): NodeSeq =
    (for {
      u <- user ?~ "User not found"
    } yield {
      snip(u)
    }): NodeSeq

  def header(xhtml: NodeSeq): NodeSeq = serve { user =>
    <div id="user-header">
      <h3>{name(xhtml)}</h3>
    </div>
  }

  def username(xhtml: NodeSeq): NodeSeq = serve { user =>
    Text(user.username.is)
  }

  def name(xhtml: NodeSeq): NodeSeq = serve { user =>
    if (user.name.is.length > 0)
      Text("%s (%s)".format(user.name.is, user.username.is))
    else
      Text(user.username.is)
  }

  def title(xhtml: NodeSeq): NodeSeq = serve { user =>
    <lift:head>
      <title lift="Menu.title">{"$name$: %*% - "+user.username.is}</title>
    </lift:head>
  }
}

object CurrentUser extends UserSnippet {
  override protected def user = User.currentUser
}

object ProfileLocUser extends UserSnippet {
  override def dispatch = super.dispatch orElse {
    case "profile" => profile
  }

  override protected def user = Site.profileLoc.currentValue

  import java.text.SimpleDateFormat

  val df = new SimpleDateFormat("MMM d, yyyy")

  def profile(xhtml: NodeSeq): NodeSeq = serve { user =>
    val editLink: NodeSeq =
      if (User.currentUser.filter(_.id.is == user.id.is).isDefined)
        <a href={Site.editProfile.url} class="btn info">Edit Your Profile</a>
      else
        NodeSeq.Empty

    val cssSel =
      "#id_name *" #> <h3>{user.name.is}</h3> &
      "#id_location *" #> user.location.is &
      "#id_whencreated" #> df.format(user.whenCreated.toDate).toString &
      "#id_editlink *" #> editLink

    cssSel.apply(xhtml)
  }
}

class UserLogin extends StatefulSnippet with Loggable {
  def dispatch = { case "render" => render }

  // form vars
  private var password = ""
  private var remember = User.loginCredentials.is.isRememberMe

  def render = {
    "#id_email [value]" #> User.loginCredentials.is.email &
    "#id_password" #> SHtml.password(password, password = _) &
    "name=remember" #> SHtml.checkbox(remember, remember = _) &
    "#id_submit" #> SHtml.onSubmitUnit(process)
  }

  private def process(): Unit = S.param("email").map(e => {
    val email = e.toLowerCase.trim
    // save the email and remember entered in the session var
    User.loginCredentials(LoginCredentials(email, remember))

    if (email.length > 0 && password.length > 0) {
      User.findByEmail(email) match {
        case Full(user) if (user.password.isMatch(password)) =>
          User.logUserIn(user, true)
          if (remember) User.createExtSession(user.id.is)
          S.seeOther(Site.home.url)
        case _ => S.error("Invalid credentials.")
      }
    }
    else if (email.length <= 0 && password.length > 0)
      S.error("Please enter an email.")
    else if (password.length <= 0 && email.length > 0)
      S.error("Please enter a password.")
    else 
      S.error("Please enter an email and password.")
  }) openOr S.error("Please enter an email address")
}

object UserTopbar {
  def render = {
    User.currentUser match {
      case Full(user) =>
        <ul class="nav secondary-nav" id="user">
          <li class="dropdown" data-dropdown="dropdown">
            <a href="#" class="dropdown-toggle">
              <span>{user.username.is}</span>
            </a>
            <ul class="dropdown-menu">
              <!--<li><lift:Menu.item name="User" donthide="true" linktoself="true">Profile</lift:Menu.item></li>-->
              <!--<li><a href={"/user/%s".format(user.username.is)}>Profile</a></li>-->
              <li><lift:Menu.item name="Account" donthide="true" linktoself="true">Settings</lift:Menu.item></li>
              <li><lift:Menu.item name="About" donthide="true" linktoself="true">Help</lift:Menu.item></li>
              <li class="divider"></li>
              <li><lift:Menu.item name="Logout" donthide="true" linktoself="true">Log Out</lift:Menu.item></li>
            </ul>
          </li>
        </ul>
      case _ if (S.request.flatMap(_.location).map(_.name).filterNot(it => List("Login", "Register").contains(it)).isDefined) =>
        <form action="/login" style="float: right">
          <button class="btn">Sign In</button>
        </form>
      case _ => NodeSeq.Empty
    }
  }
}