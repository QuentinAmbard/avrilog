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
import model.security._
import model.security.Permission._

//  val isoFormatter = ISODateTimeFormat.dateTime();
//  isoFormatter.print(datetime)
//  isoFormatter.parseDateTime(long)

object Trace extends Controller with Auth with AuthConfigImpl {

}
