package eu.henkelmann.actuarius

import utest._
import collection.SortedMap

/**
 * Tests basic parsers that are used by the more complex parsing steps.
 */

object BaseParsersTest extends TestSuite with BaseParsers {

    val tests = TestSuite {

        "The BaseParsers should parse a newline in"- {
            val p = nl
            assert(apply(p, "\n") == "\n")
            intercept[IllegalArgumentException] {
                apply(p, "\r\n")
            }
            intercept[IllegalArgumentException] {
                apply(p, "  \n")
            }
        }

        "it should parse whitespace in"- {
            val p = ws
            assert(apply(p, " ") == " ")
            assert(apply(p, "\t") == "\t")
            assert(apply(p, "    ") == "    ")
            assert(apply(p, "\t\t") == "\t\t")
            assert(apply(p, "  \t  \t  ") == "  \t  \t  ")
            //we want newlines to be treated diferrently from other ws
            intercept[IllegalArgumentException] {
                apply(p, "\n")
            }
        }

        "it should be able to look behind in"- {
            assert(apply((elem('a') ~ lookbehind(Set('a')) ~ elem('b')) ^^ { case a ~ lb ~ b => a + "" + b}, "ab") == "ab")
            intercept[IllegalArgumentException] {
                apply((elem('a') ~ lookbehind(Set('b')) ~ elem('b')) ^^ { case a ~ b => a + "" + b}, "ab")
            }

            apply(elem('a') ~ not(lookbehind(Set(' ', '\t', '\n'))) ~ '*', "a*")

        }

        "it should parse chars in ranges in"- {
            val p = ranges(SortedMap('A' -> 'Z', '0' -> '9'))
            assert(apply(p, "B") == 'B')
            assert(apply(p, "A") == 'A')
            assert(apply(p, "Z") == 'Z')
            assert(apply(p, "5") == '5')
            assert(apply(p, "0") == '0')
            assert(apply(p, "9") == '9')
            intercept[IllegalArgumentException] {
                apply(p, "a")
            }
            intercept[IllegalArgumentException] {
                apply(p, "z")
            }
            intercept[IllegalArgumentException] {
                apply(p, "<")
            }
        }

    }
}
