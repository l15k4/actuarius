package eu.henkelmann.actuarius

import utest._

/**
 * Tests the behavior of the complete parser, i.e. all parsing steps together.
 */
object TransformerTest extends TestSuite with Transformer {

  val tests = TestSuite {
    "The Transformer should create xhtml fragments from markdown in" - {
      assert(apply("") == "")
      assert(apply("\n") == "")
      assert(apply("Paragraph1\n") == "<p>Paragraph1</p>\n")
      assert(apply("Paragraph1\n\nParagraph2\n") == "<p>Paragraph1</p>\n<p>Paragraph2</p>\n")
      assert(apply("Paragraph1 *italic*\n") == "<p>Paragraph1 <em>italic</em></p>\n")
      assert(apply("\n\nParagraph1\n") == "<p>Paragraph1</p>\n")
    }

    "it should parse code blocks in" - {
      assert(apply("    foo\n") == "<pre><code>foo\n</code></pre>\n")
      assert(apply("\tfoo\n") == "<pre><code>foo\n</code></pre>\n")
      assert(apply("    foo\n    bar\n") == "<pre><code>foo\nbar\n</code></pre>\n")
      assert(apply("    foo\n  \n    bar\n") == "<pre><code>foo\n  \nbar\n</code></pre>\n")
      assert(apply("    foo\n\tbaz\n  \n    bar\n") == "<pre><code>foo\nbaz\n  \nbar\n</code></pre>\n")
      assert(apply("    public static void main(String[] args)\n") == "<pre><code>public static void main(String[] args)\n</code></pre>\n")
    }

    "it should parse paragraphs in" - {
      assert(apply(
        """Lorem ipsum dolor sit amet,
consetetur sadipscing elitr,
sed diam nonumy eirmod tempor invidunt ut
""") ==
        """<p>Lorem ipsum dolor sit amet,
consetetur sadipscing elitr,
sed diam nonumy eirmod tempor invidunt ut</p>
""")
    }

    "it should parse multiple paragraphs in"- {
      assert(apply("test1\n\ntest2\n") == "<p>test1</p>\n<p>test2</p>\n")
      assert(apply(
        """test

test

test"""
) ==
        """<p>test</p>
<p>test</p>
<p>test</p>
"""
)
    }
    "it should parse block quotes in"- {
      assert(apply("> quote\n> quote2\n") == "<blockquote><p>quote\nquote2</p>\n</blockquote>\n")
    }



    "it should parse ordered and unordered lists in"- {
      assert(apply("* foo\n* bar\n* baz\n") ==
        """<ul>
<li>foo</li>
<li>bar</li>
<li>baz</li>
</ul>
"""
        )
      assert(
        apply("+ foo\n+ bar\n+ baz\n") ==
          """<ul>
<li>foo</li>
<li>bar</li>
<li>baz</li>
</ul>
"""
)
      assert(apply("- foo\n- bar\n- baz\n") ==
        """<ul>
<li>foo</li>
<li>bar</li>
<li>baz</li>
</ul>
"""
)
      assert(apply(
        "- foo\n+ bar\n* baz\n") ==
        """<ul>
<li>foo</li>
<li>bar</li>
<li>baz</li>
</ul>
"""
)
      assert(
        apply
          ("1. foo\n22. bar\n10. baz\n") ==
          """<ol>
<li>foo</li>
<li>bar</li>
<li>baz</li>
</ol>
"""
        )
      assert(
        apply("* foo\n\n* bar\n\n* baz\n\n") ==
          """<ul>
<li><p>foo</p>
</li>
<li><p>bar</p>
</li>
<li><p>baz</p>
</li>
</ul>
"""
        )
      assert(apply("* foo\n\n* bar\n* baz\n") ==
        """<ul>
<li><p>foo</p>
</li>
<li><p>bar</p>
</li>
<li>baz</li>
</ul>
"""
        )
      assert(apply( """* foo

* bar
* baz

* bam
""")
        ==
        """<ul>
<li><p>foo</p>
</li>
<li><p>bar</p>
</li>
<li>baz</li>
<li><p>bam</p>
</li>
</ul>
"""
        )
      assert(apply(
        """* foo
        		
+ bar
- baz

* bam
""") ==
        """<ul>
<li><p>foo</p>
</li>
<li><p>bar</p>
</li>
<li>baz</li>
<li><p>bam</p>
</li>
</ul>
"""
		)
    }

    "it should stop a list after an empty line in"- {
      assert(apply( """1. a
2. b

paragraph"""
    )
        ==
        """<ol>
<li>a</li>
<li>b</li>
</ol>
<p>paragraph</p>
"""
)

    }

    "it should recursively evaluate quotes in"- {
      assert(apply("> foo\n> > bar\n> \n> baz\n") ==
        """<blockquote><p>foo</p>
<blockquote><p>bar</p>
</blockquote>
<p>baz</p>
</blockquote>
"""
        )
    }

    "it should handle corner cases for bold and italic in paragraphs in"- {
      assert(apply("*foo * bar *\n") == "<p>*foo * bar *</p>\n")
      assert(apply("*foo * bar*\n") == "<p><em>foo * bar</em></p>\n")
      assert(apply("*foo* bar*\n") == "<p><em>foo</em> bar*</p>\n")
      assert(apply("**foo* bar*\n") == "<p>*<em>foo</em> bar*</p>\n")
      assert(apply("**foo* bar**\n") == "<p><strong>foo* bar</strong></p>\n")
      assert(apply("** foo* bar **\n") == "<p>** foo* bar **</p>\n")
    }

    "it should resolve referenced links in"- {
      assert(apply( """An [example][id]. Then, anywhere
else in the doc, define the link:

  [id]: http://example.com/  "Title"
""")
        == """<p>An <a href="http://example.com/" title="Title">example</a>. Then, anywhere
else in the doc, define the link:</p>
""")
    }

    "it should parse atx style headings in"- {
      assert(apply("#A Header\n")               == "<h1>A Header</h1>\n")
        assert(apply("###A Header\n")             == "<h3>A Header</h3>\n")
        assert(apply("### A Header  \n")          == "<h3>A Header</h3>\n")
        assert(apply("### A Header##\n")          == "<h3>A Header</h3>\n")
        assert(apply("### A Header##  \n")        ==
          "<h3>A Header</h3>\n")
        assert(apply("### A Header  ##  \n")      ==
          "<h3>A Header</h3>\n")
        assert(apply("### A Header ## foo ## \n") == "<h3>A Header ## foo</h3>\n")
    }
    "it should parse setext style level 1 headings in"- {
      assert(apply("A Header\n===\n")           == "<h1>A Header</h1>\n")
        assert(apply("A Header\n=\n")             == "<h1>A Header</h1>\n")
        assert(apply(
          "  A Header \n========\n")   == "<h1>A Header</h1>\n")
        assert(apply(
          "  A Header \n===  \n")      == "<h1>A Header</h1>\n")
        assert(apply(
          "  ==A Header== \n======\n") == "<h1>==A Header==</h1>\n")
        assert(apply("##Header 1==\n=     \n")    ==
          "<h1>##Header 1==</h1>\n")
    }
    "it should parse setext style level 2 headings in"- {
        assert(apply("A Header\n---\n")           == "<h2>A Header</h2>\n")
        assert(apply("A Header\n-\n")             == "<h2>A Header</h2>\n")
        assert(apply("  A Header \n--------\n")   == "<h2>A Header</h2>\n")
        assert(apply("  A Header \n---  \n")      == "<h2>A Header</h2>\n")
        assert(apply("  --A Header-- \n------\n") == "<h2>--A Header--</h2>\n")
    }
    "it should parse xml-like blocks as is in"- {
      assert(apply("<foo> bla\nblub <bar>hallo</bar>\n</foo>\n") ==
        "<foo> bla\nblub <bar>hallo</bar>\n</foo>\n")
    }

    "it should parse fenced code blocks in"- {
      assert(apply(

        """```  foobar
System.out.println("Hello World!");
    
<some> verbatim xml </some>
    
    <-not a space-style code line
 1. not a
 2. list
    
## not a header
``` gotcha: not the end
-----------
but this is:
```         
"""    
) ==
        """<pre><code>System.out.println(&quot;Hello World!&quot;);
    
&lt;some&gt; verbatim xml &lt;/some&gt;
    
    &lt;-not a space-style code line
 1. not a
 2. list
    
## not a header
``` gotcha: not the end
-----------
but this is:
</code></pre>
"""    
)

      assert(apply(
        """```
System.out.println("Hello World!");
```
And now to something completely different.
    old style code
"""    
) ==
        """<pre><code>System.out.println(&quot;Hello World!&quot;);
</code></pre>
<p>And now to something completely different.</p>
<pre><code>old style code
</code></pre>
"""    
)

      assert(apply(
        """```
System.out.println("Hello World!");
No need to end blocks

And now to something completely different.
    old style code
"""    
) ==
        """<pre><code>System.out.println(&quot;Hello World!&quot;);
No need to end blocks

And now to something completely different.
    old style code
</code></pre>
"""    
)

      assert(apply(
        """Some text first
```
System.out.println("Hello World!");
No need to end blocks

And now to something completely different.
    old style code
"""    
) ==
        """<p>Some text first</p>
<pre><code>System.out.println(&quot;Hello World!&quot;);
No need to end blocks

And now to something completely different.
    old style code
</code></pre>
"""    
)
    }
    }
}