package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.js.JsCmds._
import code.lib._
import Helpers._
import java.util.Date
import code.model.{Click, Question, User, Session, SessionQuestions}
import code.comet.AskQ

/*
 * "/question/create_pop" links to a page that calls this render method
 * after creating a question and saving to the db, it redirects to
 * the pop question admin page
 */
object CreatePopQuestion {
	def render = {
		var question = Question.create
			.text("Pop Question")
			.saveMe
		question.rootQ(question).save

		if(User.getSession.isDefined) {
			Question.stop
			SessionQuestions.create.session(User.getSession.open_!)
				.question(question)
				.datetime(new Date)
				.save
			//Send the question to students just like any other question
			//when the teacher updates the text we will send another message
			Session.getQuestionServer(User.getSessionId).map(_ !
				AskQ(question.text, Empty, question.id, true, false))
			Click.click("?", "CREATE_POP_QUESTION", "")
			S.redirectTo("/question/pop?s=%d".format(User.getSessionId openOr "0"))
		} else { //so...it has come to this
			//you really shouldn't be able to access this w/o a session
			Click.click("?", "CREATE_POP_QUESTION", "FAIL")
			S.redirectTo("/")
		}
	}
}
