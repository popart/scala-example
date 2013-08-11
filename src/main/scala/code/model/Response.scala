package code
package model

import net.liftweb.mapper._

/*
 * A Student's response to a question
 */
object Response extends Response with LongKeyedMetaMapper[Response]

class Response extends LongKeyedMapper[Response] with IdPK {
	def getSingleton = Response

	object text extends MappedText(this) 
	object comment extends MappedText(this)
	object question extends MappedLongForeignKey(this, Question)
	object student extends MappedLongForeignKey(this, Student)
	object session extends MappedLongForeignKey(this, Session)
	object date extends MappedDateTime(this)
	object score extends MappedInt(this)
}
