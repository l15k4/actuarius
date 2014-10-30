package eu.henkelmann.actuarius

import utest._

/**
 * Tests the Line Tokenizer that prepares input for parsing.
 */
object LineTokenizerSuite extends TestSuite {
    val tests = LineTokenizerTest.tests
}
object LineTokenizerTest extends LineTokenizer {

    val tests = TestSuite {
        "The LineTokenizer should split input lines correctly in" - {
            assert(splitLines("line1\nline2\n") == List("line1", "line2"))
            assert(splitLines("line1\nline2 no nl") == List("line1", "line2 no nl"))
            assert(splitLines("test1\n\ntest2\n") == List("test1", "", "test2"))
            assert(splitLines("test1\n\ntest2\n\n") == List("test1", "", "test2"))
            assert(splitLines("\n\n").nonEmpty)
            assert(splitLines("\n").nonEmpty)
            assert(splitLines("") == List(""))
        }

        "it should preprocess the input correctly in" - {
            assert(
                tokenize("[foo]: http://example.com/  \"Optional Title Here\"") ==
                  new MarkdownLineReader(List(), Map("foo" -> new LinkDefinition("foo", "http://example.com/", Some("Optional Title Here"))))
            )

            assert(tokenize(
                """[Baz]:    http://foo.bar
'Title next line'
some text
> bla

[fOo]: http://www.example.com "A Title"
more text
[BAR]: <http://www.example.com/bla> (Also a title)"""
            ) == new MarkdownLineReader(List(
                new OtherLine("some text"),
                new BlockQuoteLine("> ", "bla"),
                new EmptyLine(""),
                new OtherLine("more text")
            ), Map(
                "bar" -> new LinkDefinition("bar", "http://www.example.com/bla", Some("Also a title")),
                "baz" -> new LinkDefinition("baz", "http://foo.bar", Some("Title next line")),
                "foo" -> new LinkDefinition("foo", "http://www.example.com", Some("A Title"))
            )))

        }

        "it should parse different line types in" - {
            def p(line: String) = {
                lineToken(new LineReader(Seq(line))) match {
                    case Success(result, _) => result
                    case e: NoSuccess => asserts.assertError("Should yield Success result", Nil)
                }
            }
            assert(p("a line") == new OtherLine("a line"))
            assert(p("    a code line") == new CodeLine("    ", "a code line"))
            assert(p("#a header#") == new AtxHeaderLine("#", "a header#"))
            assert(p("> a quote") == new BlockQuoteLine("> ", "a quote"))
            assert(p(" \t ") == new EmptyLine(" \t "))
            assert(p("* an item") == new UItemStartLine("* ", "an item"))
            assert(p("- an item") == new UItemStartLine("- ", "an item"))
            assert(p("+ an item") == new UItemStartLine("+ ", "an item"))
            assert(p("===") == new SetExtHeaderLine("===", 1))
            assert(p("---  ") == new SetExtHeaderLine("---  ", 2))
            assert(p("- - -") == new RulerLine("- - -"))
        }
    }
}