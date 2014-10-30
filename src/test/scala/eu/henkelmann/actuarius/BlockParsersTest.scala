package eu.henkelmann.actuarius

import utest._

/**
 * Tests the parsing on block level.
 */
object BlockParsersTest extends TestSuite with BlockParsers{

    val tests = TestSuite {

        "The BlockParsers should parse optional empty lines in" - {
            val p = optEmptyLines
            val el = new EmptyLine(" \n")
            assert(apply(p, Nil) == Nil)
            assert(apply(p, List(el)) == List(el))
            assert(apply(p, List(el, el)) == List(el, el))
        }

        "it should accept empty documents in" - {
            val p = markdown
            val el = new EmptyLine(" \n")
            assert(apply(p, Nil) == Nil)
            assert(apply(p, List(el)) == Nil)
            assert(apply(p, List(el, el)) == Nil)
        }

        "it should detect line types in" - {
            val p = line(classOf[CodeLine])
            assert(apply(p, List(new CodeLine("    ", "code"))) == new CodeLine("    ", "code"))
            intercept[IllegalArgumentException] {
                apply(p, List(new OtherLine("foo")))
            }
        }
    }
}