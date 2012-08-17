package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.templates._
import models._
import views._
import play.api.mvc.Results._
import jp.t2v.lab.play20.auth._
import play.api.data._
import model.Account
import security._
import security.Permission._
import model.Application

object Home extends Controller with Auth with LoginLogout with AuthConfigImpl {

  val loginForm = Form {
    mapping("email" -> email, "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def index = optionalUserAction { user =>
    implicit request =>
      val apps = user match {
        case None => Nil
        case _ => Application.findAll()
      }
      Ok(html.index(user, apps))
  }

  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  def logout = Action { implicit request =>
    gotoLogoutSucceeded.flashing(
      "success" -> "You've been logged out")
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => gotoLoginSucceeded(user.get.email))
  }

}

object Message extends Controller with Auth with AuthConfigImpl {
  def main = authorizedAction(USER) { user =>
    implicit request =>
      val apps = Application.findAll()
      val title = "message main"
      Ok(html.test(user, apps))
  }
}

trait AuthConfigImpl extends AuthConfig {

  type Id = String

  type User = Account

  type Authority = Permission

  val idManifest = classManifest[Id]

  val sessionTimeoutInSeconds = 3600

  def resolveUser(id: Id) = Account.getByEmail(id)

  def loginSucceeded[A](request: Request[A]) = Redirect(routes.Message.main)

  def logoutSucceeded[A](request: Request[A]) = Redirect(routes.Home.index)

  def authenticationFailed[A](request: Request[A]) = Redirect(routes.Home.index)

  def authorizationFailed[A](request: Request[A]) = Forbidden("no permission")

  def authorize(user: User, authority: Authority) = user.permissions.contains(authority)

  //  override def resolver[A](implicit request: Request[A]) =
  //    new CookieRelationResolver[Id, A](request)

}
