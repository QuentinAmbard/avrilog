package com.avricot.security
import org.apache.shiro.realm.Realm
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.util.ByteSource
import com.avricot.model.User
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.SimpleAuthorizationInfo
import scala.collection.JavaConversions._
import org.apache.shiro.authc.UnknownAccountException

class HbaseRealm extends AuthorizingRealm {

  override def doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo = {
    val upToken = token match {
      case token: UsernamePasswordToken => token
      case _ => throw new AuthenticationException("AuthenticationToken should be a UsernamePasswordToken")
    }
    val username = Option(upToken.getUsername());
    if (!username.isDefined) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }
    val user = User.getByEmail(username.get)
    user match {
      case None => throw new UnknownAccountException("No account found for user [" + username + "]")
      case _ => new SimpleAuthenticationInfo(username.get, user.get.password, ByteSource.Util.bytes(user.get.salt), "HbaseRealm")
    }
  }

  override def doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo = {
    //null usernames are invalid
    if (principals == null) {
      throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
    }

    val email = getAvailablePrincipal(principals) match {
      case email: String => email
      case _ => throw new AuthenticationException("principals should be a string")
    }

    val roles = User.getRole(email)
    new SimpleAuthorizationInfo(asJavaSet(roles))
  }

}