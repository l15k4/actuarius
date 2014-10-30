package eu.henkelmann.actuarius

import utest._

/**
 * tests parsing of individual lines
 */
object LineParsersTest extends TestSuite with LineParsers{

    val tests = TestSuite {
        "The LineParsers should parse horizontal rulers in" - {
            val p = ruler
            assert(apply(p, "---") == new RulerLine("---"))
            assert(apply(p, "***") == new RulerLine("***"))
            assert(apply(p, "---------") == new RulerLine("---------"))
            assert(apply(p, "*********") == new RulerLine("*********"))
            assert(apply(p, "- - -") == new RulerLine("- - -"))
            assert(apply(p, "* * *") == new RulerLine("* * *"))
            assert(apply(p, "  ---") == new RulerLine("  ---"))
            assert(apply(p, "  ***") == new RulerLine("  ***"))
            assert(apply(p, "  - - -  ----") == new RulerLine("  - - -  ----"))
            assert(apply(p, "  * * *  ****") == new RulerLine("  * * *  ****"))
        }

        "it should parse a ruler that starts like a setext header line in" - {
            val p = setext2OrRulerOrUItem
            assert(apply(p, "- - -") == new RulerLine("- - -"))
        }

        "it should parse a setext style level 1 header underline in" - {
            val p = setextHeader1
            assert(apply(p, "=") == new SetExtHeaderLine("=", 1))
            assert(apply(p, "== ") == new SetExtHeaderLine("== ", 1))
            assert(apply(p, "========== \t ") == new SetExtHeaderLine("========== \t ", 1))
        }

        "it should parse a setext style level 2 header underline in" - {
            val p = setextHeader2
            assert(apply(p, "-") == new SetExtHeaderLine("-", 2))
            assert(apply(p, "-- ") == new SetExtHeaderLine("-- ", 2))
            assert(apply(p, "---------- \t ") == new SetExtHeaderLine("---------- \t ", 2))
        }

        "it should parse an atx header line in" - {
            val p = atxHeader
            assert(apply(p, "#foo") == new AtxHeaderLine("#", "foo"))
            assert(apply(p, "## #foo##") == new AtxHeaderLine("##", " #foo##"))
        }

        "it should parse an empty line in" - {
            val p = emptyLine
            assert(apply(p, "") == new EmptyLine(""))
            assert(apply(p, "  \t ") == new EmptyLine("  \t "))
            intercept[IllegalArgumentException] {
                apply(p, " not empty ")
            }
        }

        "it should parse arbitrary lines as OtherLine tokens in" - {
            val p = otherLine
            assert(apply(p, "a line") == new OtherLine("a line"))
        }

        "it should parse quoted block lines in" - {
            val p = blockquoteLine
            assert(apply(p, "> quote") == new BlockQuoteLine("> ", "quote"))
            assert(apply(p, ">     codequote") == new BlockQuoteLine("> ", "    codequote"))
            assert(apply(p, "   >     codequote") == new BlockQuoteLine("   > ", "    codequote"))
            intercept[IllegalArgumentException] {
                apply(p, "not a quote")
            }
        }

        "it should parse unordered item start lines in" - {
            val p = uItemStartLine
            assert(apply(p, "* foo") == new UItemStartLine("* ", "foo"))
            assert(apply(p, " * foo") == new UItemStartLine(" * ", "foo"))
            assert(apply(p, "  * foo") == new UItemStartLine("  * ", "foo"))
            assert(apply(p, "   * foo") == new UItemStartLine("   * ", "foo"))
            assert(apply(p, "   *    foo") == new UItemStartLine("   *    ", "foo"))
            assert(apply(p, "   * \t  foo") == new UItemStartLine("   * \t  ", "foo"))
            assert(apply(p, "   * \t  foo  ") == new UItemStartLine("   * \t  ", "foo  "))

            intercept[IllegalArgumentException] {
                apply(p, "*foo")
            }
            intercept[IllegalArgumentException] {
                apply(p, "    * foo")
            }
            intercept[IllegalArgumentException] {
                apply(p, "1. foo")
            }

            assert(apply(p, "* foo") == new UItemStartLine("* ", "foo"))
            assert(apply(p, "+ foo") == new UItemStartLine("+ ", "foo"))
            assert(apply(p, "- foo") == new UItemStartLine("- ", "foo"))
        }


        "it should parse ordered item start lines in" - {
            val p = oItemStartLine
            assert(apply(p, "1. foo") == OItemStartLine("1. ", "foo"))
            assert(apply(p, " 12. foo") == OItemStartLine(" 12. ", "foo"))
            assert(apply(p, "  0. foo") == OItemStartLine("  0. ", "foo"))
            assert(apply(p, "   44444444. foo") == OItemStartLine("   44444444. ", "foo"))
            assert(apply(p, "   465789.    foo") == OItemStartLine("   465789.    ", "foo"))
            assert(apply(p, "   4455. \t  foo") == OItemStartLine("   4455. \t  ", "foo"))
            assert(apply(p, "   9. \t  foo  ") == OItemStartLine("   9. \t  ", "foo  "))

            intercept[IllegalArgumentException] {
                apply(p, "1.foo")
            }
            intercept[IllegalArgumentException] {
                apply(p, "    1. foo")
            }
            intercept[IllegalArgumentException] {
                apply(p, "* foo")
            }
        }

        "it should parse link definitions in" - {
            val p = linkDefinitionStart
            assert(
                apply(p, "[foo]: http://example.com/  \"Optional Title Here\"") ==(new LinkDefinitionStart("foo", "http://example.com/"), Some("Optional Title Here"))
            )
            assert(apply(p, "[foo]: http://example.com/") ==(new LinkDefinitionStart("foo", "http://example.com/"), None))
            assert(apply(p, "[Foo]: http://example.com/  'Optional Title Here'") ==(new LinkDefinitionStart("foo", "http://example.com/"), Some("Optional Title Here")))
            assert(apply(p, "[Foo]: http://example.com/?bla=<>  (Optional Title Here)") ==(new LinkDefinitionStart("foo", "http://example.com/?bla=&lt;&gt;"), Some("Optional Title Here")))
            assert(apply(p, "[Foo]: http://example.com/?bla=<>  (Optional Title Here)") ==(new LinkDefinitionStart("foo", "http://example.com/?bla=&lt;&gt;"), Some("Optional Title Here")))
        }

        "it should parse link titles in" - {
            val p = linkDefinitionTitle
            assert(apply(p, "  (Optional Title Here)  ") == "Optional Title Here")
        }

        "it should parse openings of fenced code blocks in" - {
            val p = fencedCodeStartOrEnd
            assert(apply(p, "```") ==
              new FencedCode("```"))
            assert(apply(p, "   ```\t") ==
              new FencedCode("   ```\t"))
            assert(apply(p, "  ``` \t ") ==
              new FencedCode("  ``` \t "))
            assert(apply(p, "  ``` \t java  \t ") ==
              new ExtendedFencedCode("  ``` \t ", "java  \t "))
        }
    }
}