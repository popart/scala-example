package code
package model

import net.liftweb.mapper._
import net.liftweb.mapper.{By_>, By_<}
import net.liftweb.common._
import net.liftweb.http.S
import code.comet._

object Question extends Question with LongKeyedMetaMapper[Question] with Loggable {
	//Clears all the comet servers
	def stop(): Boolean = {
		if(!User.getSession.isDefined){
			logger.debug("[ERROR] Stopping a question but no class in session")
			S.error("No class in session")
			false
		}
		else {
			Session.getQuestionServer(User.getSessionId).map(_ ! AskQ("", Empty, -1, false, false))
			Session.getResponseServer(User.getSessionId).map(_ ! ResponseServerUpdate(Nil))
			Session.getDisplayServer(User.getSessionId).map(_ ! DisplayServerUpdate(Nil))
			//Session.getTimerServer(User.getSessionId).map(_ ! StopTime)
			Session.getResponseCloudServer(User.getSessionId).map(_ ! Refresh)
			logger.debug("Question has been stopped, Session:"+(User.getSessionId openOr "no user session"))
			true
		}
	}
}

class Question extends LongKeyedMapper[Question] with OneToMany[Long, Question] with ManyToMany with IdPK {
	val maxDispLength = 50
	def getSingleton = Question

	def blockDelete = false
	def mostRecent_? = childQ.isEmpty // || childQ == this
	def mostRecent():Question = {
		if(childQ.isEmpty /*|| childQ == this*/) this
	  else childQ.open_!.mostRecent
	}
	//Used for Question Tree's ajax loading
	def jsonsafetify(s: String): String = {
		s.replace("\"", "&quot;")
		.replace("\r", " ")
		.replace("\n", " ")
		.replace("<", "&#60;")
		.replace(">", "&#62")
	}
	def shortText:String = if(text.is.length < maxDispLength) text.is else text.is.substring(0, maxDispLength-3) + "..."
	def shortTextJSON:String = "<a href='#' onclick='window.open(&#34;/question/view/%s&#34;)'>".format(primaryKeyField.toString)+jsonsafetify(shortText)+"</a>"
	def shortText(maxLength:Int): String = if(text.is.length < maxLength) text.is else text.is.slice(0, maxLength-3) + "..."
	//shows old versions of this question
	def history = {
		def historyTail(q:Question, store:List[Question]): List[Question] = {
			if(q.parentQ.isEmpty || q.parentQ.open_! == q) q::store
			else historyTail (q.parentQ.open_!, q::store)
		}
		(historyTail(this, Nil) reverse) tail
	}
	//create a new question and link it to this one for history
	def update(newText:String) = {
		var	newQ = Question.create.parentQ(this).text(newText)
		if(folder.isDefined) newQ.folder(folder.open_!)
		if(imageInfo.isDefined) newQ.imageInfo(imageInfo.open_!)
		newQ.rootQ(this.rootQ)
		newQ.order(this.order)
		newQ.save
		this.childQ(newQ).save; newQ
	}
	//move questions up and down within a folder, for teacher organization
	def bumpOrder(up: Boolean) {
		val curOrder = this.order.is
		if(up) {
			var upperQs = Question.findAll(By_<(Question.order, curOrder), NullRef(Question.childQ), By(Question.folder, this.folder), OrderBy(Question.order, Descending))
			if(!upperQs.toList.isEmpty) {
				var swap = upperQs.toList.head
				this.order(swap.order).save
				swap.order(curOrder).save
			} else {
				this.order(curOrder - 1).save
			}
		} else {
			val lowerQs = Question.findAll(By_>(Question.order, curOrder), NullRef(Question.childQ), By(Question.folder, this.folder), OrderBy(Question.order, Ascending))
			if(!lowerQs.toList.isEmpty) {
				var swap = lowerQs.toList.head
				this.order(swap.order).save
				swap.order(curOrder).save
			} else {
				this.order(curOrder + 1).save
			}
		}
	}
	object text extends MappedTextarea(this, 512)
	object folder extends MappedLongForeignKey(this, Folder)
	object parentQ extends MappedLongForeignKey(this, Question)
	object childQ extends MappedLongForeignKey(this, Question)
	//A history of questions all share one root to easily see if they are connected
	object rootQ extends MappedLongForeignKey(this, Question)
	object responses extends MappedOneToMany(Response, Response.question, OrderBy(Response.student, Ascending))
	object imageInfo extends MappedLongForeignKey(this, ImageInfo)
	object order extends MappedInt(this) {
		override def defaultValue = 0
	}
}
