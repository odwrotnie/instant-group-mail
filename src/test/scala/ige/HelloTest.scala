package ige

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

class HelloTest
  extends FlatSpec
  with LazyLogging {

  "2 plus 2" should "equal 4" in {
    IGE.start
  }
}
