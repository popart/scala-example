package code
package model

import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.common._
import code.lib.MySessionInfo
import code.comet.{LoginServer, LoginLine, DisplayServer, QuestionServer, ResponseCloudServer, ResponseServer, LoginServerUpdate, AskQ}

/*
 * A class session that runs and saves student responses
 * I know this is confusing name w/ web session
 */
object Session extends Session with LongKeyedMetaMapper[Session] with Loggable {
	private var runningSessions:Map[Long, Session] = Map()
	private var loginServers:Map[Long, LoginServer] = Map()
	private var displayServers:Map[Long, DisplayServer] = Map()
	private var questionServers:Map[Long, QuestionServer] = Map()
	private var responseServers:Map[Long, ResponseServer] = Map()
	private var responseCloudServers:Map[Long, ResponseCloudServer] = Map()
//	private var timerServers:Map[Long, TimerServer] = Map()

	//use these methods so different teachers can each have their own comets
	def getLoginServer(id: Box[Long]) = loginServers.get(id openOr -1L)
	def getQuestionServer(id: Box[Long]) = questionServers.get(id openOr -1L)
	def getDisplayServer(id: Box[Long]) = displayServers.get(id openOr -1L)
	def getResponseServer(id: Box[Long]) = responseServers.get(id openOr -1L)
	def getResponseCloudServer(id: Box[Long]) = responseCloudServers.get(id openOr -1L)
//	def getTimerServer(id: Box[Long]) = timerServers.get(id openOr -1L)

	def addSession(s: Session) = {
		val key = s.primaryKeyField.is
		runningSessions += key -> s
		loginServers += key -> new LoginServer
		displayServers += key -> new DisplayServer
		questionServers += key -> new QuestionServer
		responseServers += key -> new ResponseServer
		responseCloudServers += key -> new ResponseCloudServer
//		timerServers += key -> new TimerServer
		MySessionInfo.beginClassSession(s) //store session in a timeout manager
		logger.info("Added Session: "+key.toString)
	}
	//this MUST get called to prevent memory leaks
	def removeSession(s: Session) = {
		val key = s.primaryKeyField.is
		logger.debug("Begin Session.removeSession:"+key.toString)
		getQuestionServer(Full(key)).map(_ ! AskQ("LOGOUT", Empty, -1, true, false))
		logger.debug("Sent session close comet messages")
		runningSessions -= key
		loginServers -= key
		displayServers -= key
		questionServers -= key
		responseServers -= key
		responseCloudServers -= key
		//timerServers -= key
		logger.info("End Session.removeSession: "+key.toString)
	}
	/* used so students can log into the correct session */
	def findStudentSession(std: Student): Option[Session] = {
		val matchedSessions = runningSessions.values.filter(s => std.section.toLong == s.section.toLong)
		matchedSessions.size match {
			case 1 => Some(matchedSessions.toList(0))
			case 0 => None
			case _ => {
				logger.warn("[WARNING] Duplicate sessions running for section id: "+std.section.toLong)
				None
			}
		}
	}
	/* Used for killing extra user sessions */
	def findUserSessions(u: User) = runningSessions.values.filter(s =>
		s.section.map(_.teacher == u) openOr false)

	/* Log student into a running session for the section that they are a member of */
	def managedLoginStudent(s: Student): Boolean = {
		val session = findStudentSession(s)
		session match {
			case Some(sesh) => {
				sesh.loginStudent(s)
				true
			}
			case None => {
				logger.info("Could not login Student:{id:"+s.primaryKeyField.toString+", name:"+s.lastName.is+", section id: "+s.section.toString+"}, Running Sessions: "+runningSessions.toString)
				false
			}
		}
	}

	/* Log the student out from the correct session */
	def managedLogoutStudent(s: Student) = {
		findStudentSession(s) match {
			case Some(sesh) => {
				sesh.logoutStudent(s.primaryKeyField.toString)
				logger.info("Logged out Student:{"+s.primaryKeyField.toString+", "+s.lastName.is+"} to Session: "+sesh.primaryKeyField.is)
			}
			case None => logger.info("Could not logout Student:{"+s.primaryKeyField.toString+", "+s.lastName.is+"}, no session")
		}
	}
}	

class Session extends LongKeyedMapper[Session] with ManyToMany with IdPK {
	var loggedInStudents:List[Student] = Nil

	def loginStudent(s: Student) = {
		Student.storeSession(this)
		loggedInStudents ::= s
		Session.getLoginServer(User.getSessionId).map(_ ! LoginLine(s.displayName, true))
	}
	/* Called when a teacher ends a class session, so those false LoginLines
	 * are kind of useless, since students don't log themselves out 
	 * This should get called on a student's timeout though but that takes 55 min*/
	def logoutStudent(sid: String) = {
			val s= Student.find(By(Student.userId, sid)) openOr Student.create
			loggedInStudents -= s
			//Session.getLoginServer(User.getSessionId).map(_ ! LoginLine(s.displayName, false))
			//Session.getLoginServer(User.getSessionId).map(_ ! LoginLine("logout success: "+s.displayName, true))
			Student.clearSession(false)
	}
	def getSingleton = Session
	
	object questions extends MappedManyToMany(SessionQuestions, SessionQuestions.session, SessionQuestions.question, Question)
	object section extends MappedLongForeignKey(this, Section)
	object date extends MappedDate(this)
	object title extends MappedString(this, 128)
}

/* Many-to-many mapping */
object SessionQuestions extends SessionQuestions with LongKeyedMetaMapper[SessionQuestions]
class SessionQuestions extends LongKeyedMapper[SessionQuestions] with IdPK {
	def getSingleton = SessionQuestions
	object session extends MappedLongForeignKey(this, Session)
	object question extends MappedLongForeignKey(this, Question)
	object datetime extends MappedDateTime(this)
}
