package code
package model

import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util._
import scala.xml._

/*
 * Serves ImageBlobs
 * Important to delete ImageInfo with its Blob
 * All this image serving stuff comes from github.com/jimwise/shared/tree/master/gallery
 */
object ImageInfo extends ImageInfo with LongKeyedMetaMapper[ImageInfo] {
	private object cache extends RequestMemoize[String, Box[ImageInfo]]
	private def findFromRequest(req: Req): Box[ImageInfo] = {
		val toFind = req.path.wholePath.last
		cache.get(toFind, find(By(name, toFind)))
	}
	def serveImage: LiftRules.DispatchPF = {
		case req @ Req("img"::_::Nil, _, GetRequest) if findFromRequest(req).isDefined =>
			() => {
				val info = findFromRequest(req).open_!
				info.blob.obj.map(blob => 
					InMemoryResponse(blob.image, List(("some header", "like mime-type")), Nil, 200))
			}
	}

	def choices = ImageInfo.findAll.map({i => (i.id.toString, i.name.toString)})
}

class ImageInfo extends LongKeyedMapper[ImageInfo] with IdPK {
	def getSingleton = ImageInfo
	
	object name extends MappedPoliteString(this, 256) {
		override def dbIndexed_? = true
		override def defaultValue = ""

		private def noSlashes(s: String): List[FieldError] =
			if (s.contains("/"))
				List(FieldError(this, Text("Image name \""+s+"\" may not contain slashes")))
			else Nil

		override def validations =
			valMinLen(1, "Image name must not be empty") _ ::
			valUnique("Image with same name already exists") _ ::
			noSlashes _ ::
			super.validations
	}
	object mimeType extends MappedPoliteString(this, 128)
	object blob extends MappedLongForeignKey(this, ImageBlob)
	object question extends MappedLongForeignKey(this, Question)

	def deleteWithBlob {
		this.blob.obj match {
			case Full(x) => x.delete_!
			case _ =>
		}
		this.delete_!
	}

	def url = "/img/" + name
}
