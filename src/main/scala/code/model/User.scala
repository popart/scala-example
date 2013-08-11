package code
package model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http._
import js.JsCmds._
import S._
import net.liftweb.sitemap._
import Loc._
import scala.xml.{NodeSeq, Atom}

/**
 * Represents a teacher
 * You need to checkout the framework src for this one
 * There is a superAdmin field for you that allows creating new users
 * - and also access loglevel/change (widget to change logger settings within app)
 */
object User extends User with MetaMegaProtoUser[User] with SessionHolder {
	override def onClearSession = {logger.debug("Clearing a Teacher Session")}
	override def homePage = "/section/index"
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
			       <lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email,
  locale, timezone, password)
	override val basePath = "teacher" :: Nil

  // comment this line out to require email validations
  override def skipEmailValidation = true
	
	/*Create User - start*/
	override def signUpSuffix = "create"
  override def signupXhtml(user: TheUserType) = {
    (<form method="post" action={S.uri}><table><tr><td
              colspan="2">Create User</td></tr>
          {localForm(user, false, signupFields)}
          <tr><td>&nbsp;</td><td><user:submit/></td></tr>
                                        </table></form>)
  }
	override def createUserMenuLocParams:List[LocParam[Unit]] = 
		Hidden :: testSuperUser :: Template(() => wrapIt(signupFunc.map(_()) openOr signup)) :: Nil
  override protected def actionsAfterSignup(theUser: TheUserType, func: () => Nothing): Nothing = {
    theUser.setValidated(skipEmailValidation).resetUniqueId()
    theUser.save
    if (!skipEmailValidation) {
      sendValidationEmail(theUser)
      S.notice(S.??("sign.up.message"))
      func()
    } else {
			S.notice(S.??("User created"))
			func()
    }
  }
	/*Create User - end*/
	
	override def loginMenuLocParams:List[LocParam[Unit]] = If(() => !Student.loggedIn_?, () => RedirectResponse("/")) :: LocGroup("main") :: super.loginMenuLocParams
	override def loginMenuLoc: Box[Menu] = Full(Menu(Loc("Teacher Login", loginPath, S.??("Teacher Login"), loginMenuLocParams)))
	override def validateUser(email: String) = findUserByUserName(email) match {
		case Full(user) if !user.validated_? =>
			user.setValidated(true).resetUniqueId().save
			new Atom()
		case _ => new Atom() 
	}

	override def lostPasswordMenuLocParams:List[LocParam[Unit]] = Hidden :: super.lostPasswordMenuLocParams
	override def editUserMenuLocParams:List[LocParam[Unit]] = LocGroup("user") :: super.editUserMenuLocParams
	override def changePasswordMenuLocParams:List[LocParam[Unit]] = LocGroup("user") :: super.changePasswordMenuLocParams
	override def logoutMenuLocParams:List[LocParam[Unit]] = LocGroup("user") :: super.logoutMenuLocParams

	/* Clear running class sessions when a user logs out */
	def killAllSessionsBoxed(b: Box[User]) = {
		b.map(u => killAllSessions(u))
	}	
	def killAllSessions(u: User) = {
		logger.debug("Teacher killAllSessions")
		Session.findUserSessions(u).map({s =>
			logger.debug("Call removeSession - User:"+u.primaryKeyField.toString+
						", Session: "+s.primaryKeyField.toString)
			Session.removeSession(s)
		})
	}
	//these are just hooks for extra login actions
	onLogOut = {
		logger.debug("Calling Teacher logout functions")
		List(killAllSessionsBoxed(_), (_) => Click.click("?", "LOGOUT", ""))
	}
	onLogIn = {
		logger.debug("Calling Teacher login functions")
		List(killAllSessions(_), (_) => Click.click("Teacher Login", "LOGIN", "")) 
	}
}

class User extends MegaProtoUser[User] with OneToMany[Long, User]	{
  def getSingleton = User // what's the "meta" server

	object sections extends MappedOneToMany(Section, Section.teacher, OrderBy(Section.title, Ascending))
	object folders extends MappedOneToMany(Folder, Folder.teacher, OrderBy(Folder.title, Ascending))
}

