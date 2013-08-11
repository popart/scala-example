package code
package model

import net.liftweb.http._
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.mockweb.MockWeb
import code.lib._
import net.liftweb.mapper._
import Helpers._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.xml._

class QuestionSpec extends Spec with ShouldMatchers with SessionContext {
  describe("A question") {
		it ("should point to its parent upon updating") {
			val q = Question.create.saveMe
			val update = q.update("new")		
			update.parentQ should equal (q)
		}
		it ("should correctly list past versions") {
			var curUpdate = Question.create.saveMe
			var updates = List(curUpdate)
			List("new1", "new2", "new3").map(s => {
				val update = curUpdate.update(s)		
				update :: updates
				curUpdate = update
			})
			updates.head.history should equal (updates.tail)
		}
		it ("should point all updates to the same root") {
			var curUpdate = Question.create.saveMe
			var updates = List(curUpdate)
			List("new1", "new2", "new3").map(s => {
				val update = curUpdate.update(s)		
				update :: updates
				curUpdate = update
			})
			updates.filter(u => u.rootQ == updates.head.rootQ) should have size (updates.size)
		}
  }
}
