package code
package lib

/**
 * Created by IntelliJ IDEA.
 * User: dpp
 * Date: 2/14/12
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */

import code.model._

import net.liftweb._
import http._
import common._
import util._
import Helpers._

case class DisSessionInfo(id: String, user: Box[Long] = Empty, started: Long = millis, lastSeen: Long = millis)

object MySessionInfo extends Loggable {
  private var sessions: Map[String, DisSessionInfo] = Map()
	private var classSessions: Map[String, Session] = Map()
  private var users: Map[Long, Set[String]] = Map()


  def doBeginService(session: LiftSession, req: Req) {
    val id = session.uniqueId
    synchronized {
      for {
        cur <- sessions.get(id)
      } sessions += id -> cur.copy(lastSeen = millis)
    }
  }

  def created(session: LiftSession,req: Req) {
    synchronized {
      val id = session.uniqueId
      sessions += id -> DisSessionInfo(id)
    }
  }

  def destroyed(session: LiftSession) {
    synchronized {
      val id = session.uniqueId
      for {
        cur <- sessions.get(id)
        user <- cur.user
      } {
        val newSet = (users.get(user) getOrElse Set()) - id
        if (newSet.isEmpty) {
          users = users - user
        } else {
          users += user -> newSet
        }
      }
      for {
        info <- sessions.get(id)
      } {
				logger.debug("Destroying session "+id+" first seen at "+(new java.util.Date(info.started))+
      		" most recently seen at "+(new java.util.Date(info.lastSeen)))
				sessions = sessions - id
			}
			for {
				classSession <- classSessions.get(id)
			} {
				Session.removeSession(classSession)
				classSessions = classSessions - id
			}
    }
  }

  def login(user: User) {
    synchronized {
      val userId = user.id.is
      for {
        sess <- S.session
      } {
        val id = sess.uniqueId
        val oldSet = (users.get(userId) getOrElse Set())
        val newSet = oldSet + id
        users += userId -> newSet

        // destroy the old sessions on a separate thread
        for {
          s <- oldSet
          theSession <- SessionMaster.getSession(s, Empty)
        }
          Schedule(() => {
            logger.debug("Destroying "+theSession.uniqueId+" because of duplicate login")
            theSession.destroySession()
          })
      }
    }
  }

	def beginClassSession(classSession: Session) {
    synchronized {
      for {
        sess <- S.session
      } {
        val id = sess.uniqueId
        classSessions += id -> classSession
      }
    }
	}
}
