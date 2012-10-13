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
import model.Application
import model.security._
import model.security.Permission._

object App extends Controller with Auth with AuthConfigImpl {

  val appForm = Form {
    mapping("id" -> text, "name" -> text, "secret" -> text)((id, _, _) => Application.findById(id))(_.map(app => (app.id, app.name, app.secret)))
      .verifying("Invalid name", result => result.isDefined)
  }

  //  def read = Action { implicit request =>
  //    appForm.bindFromRequest.fold(
  //      formWithErrors => BadRequest(html.application.application(formWithErrors)),
  //      app => Redirect(routes.Message.main)
  //  }

}
