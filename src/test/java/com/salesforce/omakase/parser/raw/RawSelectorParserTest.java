/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.omakase.parser.raw;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.parser.AbstractParserTest;
import com.salesforce.omakase.test.util.TemplatesHelper.SourceWithExpectedResult;
import org.junit.Test;

import java.util.List;

import static com.salesforce.omakase.test.util.TemplatesHelper.withExpectedResult;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link RawSelectorParser}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class RawSelectorParserTest extends AbstractParserTest<RawSelectorParser> {

    @Override
    public List<String> invalidSources() {
        return ImmutableList.of(
            "   ",
            "\n",
            "{color: red}",
            "1234",
            "$123",
            "$class");
    }

    @Override
    public List<String> validSources() {
        return ImmutableList.of(
            ".class1",
            ".class1.class2",
            ".class1 .class2",
            ".class1 + .class2",
            "#id #id",
            "p p",
            "A",
            ":before",
            "*",
            "::after",
            "  .class .class",
            "  p#id",
            " a[href]",
            "div[class]",
            "E[foo=\"bar\"]",
            "E[foo~=\"bar\"]",
            "E[foo^=\"bar\"]",
            "E[foo$=\"bar\"]  ",
            "E[foo*=\"bar\"]",
            "[hidden]",
            "[foo=\"bar\"]",
            "E:nth-child(n)",
            "E::first-letter",
            "p+p",
            "\t  p~p",
            "\n  p~p",
            "p\n.class\n*#id",
            "p~ a",
            ".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa",
            ".aclajsclkajsclajsca>.ahcjashjkchas___>._afaafa_fafa#afa-afa-afaf-afa",
            "/*comment*/.class",
            "/*comment\n\n aaffa1*//*comment*/.class",
            "/*comment\n\n aaffa1*/\n /*comment*/.class",
            ".class/*comment*/ .class",
            ".class /*comment*/.class",
            ".class/*comment*/",
            ".class /*comment*/"
        );
    }

    @Override
    public List<SourceWithExpectedResult<Integer>> validSourcesWithExpectedEndIndex() {
        return ImmutableList.of(
            withExpectedResult(".class1, .class2", 7),
            withExpectedResult(".class1 + .class2 { color: red }", 18),
            withExpectedResult("div[class]", 10),
            withExpectedResult("p\n.class\n*#id", 13),
            withExpectedResult(".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa", 70),
            withExpectedResult("E:nth-child(n), .class", 14),
            withExpectedResult("E[foo=\"b,ar\"], .class", 13),
            withExpectedResult("E[foo=\"b{a r\"]#id, #id", 17));
    }

    @Override
    public String validSourceForPositionTesting() {
        return Iterables.get(validSources(), 0);
    }

    @Override
    public boolean allowedToTrimLeadingWhitespace() {
        return true;
    }

    @Test
    @Override
    public void matchesExpectedBroadcastContent() {
        List<ParseResult<String>> results = parseWithExpected(
            withExpectedResult(".class1, .class2", ".class1"),
            withExpectedResult(".class1 + .class2 { color: red }", ".class1 + .class2"),
            withExpectedResult("div[class]", "div[class]"),
            withExpectedResult("p\n.class\n*#id", "p\n.class\n*#id"),
            withExpectedResult(
                ".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa",
                ".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa"),
            withExpectedResult("E:nth-child(n), .class", "E:nth-child(n)"),
            withExpectedResult("afafjafasfkasfkjsa", "afafjafasfkasfkjsa"),
            withExpectedResult("E[foo=\"b,ar\"]", "E[foo=\"b,ar\"]"),
            withExpectedResult("E[foo=\"b{a r\"]#id", "E[foo=\"b{a r\"]#id"),
            withExpectedResult("/*comment*/.class.class2", ".class.class2"),
            withExpectedResult("/*comment*//*comment*/#id-abc_ac", "#id-abc_ac"),
            withExpectedResult(".class/*comment*/.class2", ".class/*comment*/.class2"),
            withExpectedResult(".class.class2/*comment*/, .class2", ".class.class2/*comment*/"),
            withExpectedResult(".class.class2,/*comment*/ .class2", ".class.class2")
        );

        for (ParseResult<String> result : results) {
            Selector s = result.broadcaster.findOnly(Selector.class).get();
            assertThat(s.raw().get().content()).isEqualTo(result.expected);
        }
    }

    @Test
    public void correctLineAndColumnNumber() {
        GenericParseResult result = parse("\n  .class1").get(0);
        Syntax syntax = Iterables.get(result.broadcastedSyntax, 0);
        assertThat(syntax.line()).isEqualTo(2);
        assertThat(syntax.column()).isEqualTo(3);
    }

    @Test
    public void storesComments() {
        Selector s = parse("/*comment1*/.class.class").get(0).broadcaster.findOnly(Selector.class).get();
        assertThat(s.comments().get(0).content()).isEqualTo("comment1");

        s = parse("/*comment1\n * new line *//*comment2*/.class.class").get(0).broadcaster.findOnly(Selector.class).get();
        assertThat(s.comments()).hasSize(2);
    }
}
