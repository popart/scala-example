package code
package model

import net.liftweb.mapper._

/*
 * A class section, such as "Period 1"
 */
object Section extends Section with LongKeyedMetaMapper[Section]

class Section extends LongKeyedMapper[Section] with OneToMany[Long, Section] with IdPK {
	def getSingleton = Section

	object title extends MappedString(this, 64)
	//ORDERBY is now broken b/c lastName is encrypted...
	object students extends MappedOneToMany(Student, Student.section, OrderBy(Student.lastName, Ascending))
	object teacher extends MappedLongForeignKey(this, User)
}
