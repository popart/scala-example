package code 
package snippet 

import net.liftweb.mapper.{By, NotNullRef}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{ImageInfo, ImageBlob, Folder, Question, User, Click}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JsCmds._
import js.JE.JsRaw
import java.io.{File, FileOutputStream}

/*
 * code courtesy of github.com/jimwise/shared/tree/master/gallery
 */
class QuestionImage(qi: QuestionId) {
	var fileHolder: Box[FileParamHolder] = Empty

	def upload = {
		def deleteImage() = {
			var q = Question.findByKey(qi.theId.toLong) openOr Question.create
			q.imageInfo.map(_.deleteWithBlob)
			Click.click("Question View", "DELETE_IMAGE", "")
			S.redirectTo("/question/view/"+qi.theId)
		}

		def saveImage() = {
			val uploadOk = fileHolder match {
				case Full(FileParamHolder(_, null, _, _)) => false
				case Full(FileParamHolder(_, mime, filename, data))
					if mime.startsWith("image/") => {
						val blob = ImageBlob.create.image(data)
						val img = ImageInfo.create
							.name(filename)
							.mimeType(mime)
						img.validate match {
							case Nil =>
								blob.saveMe
								img.blob(blob)
								img.saveMe
								var q = Question.findByKey(qi.theId.toLong) openOr Question.create
								q.imageInfo.map(_.deleteWithBlob)
								q.imageInfo(img).save
								Click.click("Question View", "UPLOAD_IMAGE", "")
								S.notice("Image Uploaded")
							case err =>
								Click.click("Question View", "UPLOAD_IMAGE", "FAIL")	
								S.error(err)
						}
						true
					}
				case Full(_) => false
				case _ => false
			}
			if(!uploadOk) S.error("fail: "+(fileHolder.map(_.fileName) openOr "Image upload failed"))
			S.redirectTo("/question/view/"+qi.theId)
		}

		"#img_upload" #> SHtml.fileUpload(f => fileHolder = Full(f)) &
		"#img_submit" #> SHtml.submit("Add Image", saveImage) &
		"#img_delete" #> SHtml.submit("Delete Image", deleteImage)
	}
}
