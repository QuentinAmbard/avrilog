package model.security

trait Permission {
}
object Permission {
  def valueOf(name: String): Permission = {
    name match {
      case "ADMIN" => ADMIN
      case "USER" => USER
    }
  }
  case object ADMIN extends Permission
  case object USER extends Permission
}
