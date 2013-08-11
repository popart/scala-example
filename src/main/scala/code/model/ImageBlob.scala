package code
package model

import net.liftweb.mapper._

/*
 * Stores images as binaries in the db
 */
object ImageBlob extends ImageBlob with LongKeyedMetaMapper[ImageBlob] {
}

class ImageBlob extends LongKeyedMapper[ImageBlob] with IdPK {
	def getSingleton = ImageBlob
	
	object image extends MappedBinary(this)
}


