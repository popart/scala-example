package code
package model

import net.liftweb.mapper._

/*
 * A simple directory structure for Questions
 */
object Folder extends Folder with LongKeyedMetaMapper[Folder]

class Folder extends LongKeyedMapper[Folder] with OneToMany[Long, Folder] with IdPK {
	def getSingleton = Folder
	// Old versions of questions are kept for historical purposes
	// so call 'currentQuestions' to get only the latest versions
	def currentQuestions:List[Question] = questions.filter(_.mostRecent_?).toList
	object title extends MappedString(this, 32) {
		override def setFilter = trim _ :: super.setFilter
	}
	object parent extends MappedLongForeignKey(this, Folder)
	object children extends MappedOneToMany(Folder, Folder.parent)
	object questions extends MappedOneToMany(Question, Question.folder, OrderBy(Question.order, Ascending))
	object teacher extends MappedLongForeignKey(this, User)
}
