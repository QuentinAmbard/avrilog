package bootstrap.liftweb

import net.liftweb._
import common._
import http.S
import sitemap._
import sitemap.Loc._
import com.avricot.model.User
import shiro.sitemap.Locs._
import shiro.Shiro

object MenuGroups {
  val SettingsGroup = LocGroup("settings")
  val TopBarGroup = LocGroup("topbar")
}

/*
 * Wrapper for Menu locations
 */
case class MenuLoc(menu: Menu) {
  lazy val url: String = S.contextPath + menu.loc.calcDefaultHref
  lazy val fullUrl: String = S.hostAndPath + menu.loc.calcDefaultHref
}

object Site {
  import MenuGroups._

  // locations (menu entries)
  val home = MenuLoc(Menu.i("Home") / "index" >> TopBarGroup >> RequireAuthentication)
  //login/logout with mongoAuth
  //  private val profileParamMenu = Menu.param[User]("User", "Profile",
  //    User.findByUsername _,
  //    _.username.is) / "user" >> RequireAuthentication >> Loc.CalcValue(() => User.currentUser)
  //  lazy val profileLoc = profileParamMenu.toLoc

  val password = MenuLoc(Menu.i("Password") / "settings" / "password" >> RequireAuthentication)
  val account = MenuLoc(Menu.i("Account") / "settings" / "account" >> RequireAuthentication)
  val editProfile = MenuLoc(Menu("EditProfile", "Profile") / "settings" / "profile" >> RequireAuthentication)

  //val register = MenuLoc(Menu.i("Register") / "register" >> RequireNotLoggedIn)
  //val login = MenuLoc(Menu.i("Register") / "register" >> RequireNotLoggedIn)

  private def menus = List(
    home.menu,
    Menu.i("Login") / "login" >> DefaultLogin >> RequireNoAuthentication,
    //    loginToken.menu,
    //    logout.menu,
    //    profileParamMenu,
    //    account.menu,
    //    password.menu,
    //    editProfile.menu,
    Menu.i("Application") / "application" / ** >> RequireAuthentication,
    Menu.i("About") / "about" >> TopBarGroup,
    Menu.i("Contact") / "contact" >> TopBarGroup,
    Menu.i("Throw") / "throw" >> Hidden,
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden) ::: Shiro.menus

  /*
   * Return a SiteMap needed for Lift
   */
  def siteMap: SiteMap = SiteMap(menus: _*)
}