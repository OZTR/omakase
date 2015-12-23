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

package com.salesforce.omakase.parser.refiner;

import com.salesforce.omakase.Message;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.Statement;
import com.salesforce.omakase.ast.StatementIterable;
import com.salesforce.omakase.ast.atrule.AbstractAtRuleMember;
import com.salesforce.omakase.ast.atrule.AtRule;
import com.salesforce.omakase.ast.atrule.AtRuleBlock;
import com.salesforce.omakase.ast.atrule.AtRuleExpression;
import com.salesforce.omakase.ast.atrule.MediaQueryList;
import com.salesforce.omakase.ast.collection.SyntaxCollection;
import com.salesforce.omakase.broadcast.QueryableBroadcaster;
import com.salesforce.omakase.parser.ParserException;
import com.salesforce.omakase.writer.StyleAppendable;
import com.salesforce.omakase.writer.StyleWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Iterator;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link MediaRefiner}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class MediaRefinerTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    MediaRefiner strategy;
    QueryableBroadcaster broadcaster;
    MasterRefiner refiner;

    @Before
    public void setup() {
        strategy = new MediaRefiner();
        broadcaster = new QueryableBroadcaster();
        refiner = new MasterRefiner(broadcaster).register(strategy);
    }

    @Test
    public void returnsFalseIfNotApplicable() {
        AtRule ar = new AtRule(1, 1, "blah", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.NONE);
        assertThat(ar.isRefined()).isFalse();
    }

    @Test
    public void doesntRefineExpressionIfAlreadyRefined() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        TestExpression expression = new TestExpression();
        ar.expression(expression);
        Refinement refinement = strategy.refine(ar, broadcaster, refiner);
        assertThat(ar.expression().get()).isSameAs(expression);
    }

    @Test
    public void doesntRefineBlockIfAlreadyRefined() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        TestBlock block = new TestBlock();
        ar.block(block);
        strategy.refine(ar, broadcaster, refiner);
        assertThat(ar.block().get()).isSameAs(block);
    }

    @Test
    public void returnsTrueIfRefinedExpressionOnly() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        ar.block(new TestBlock());
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.FULL);
        assertThat(broadcaster.find(AtRuleExpression.class).isPresent()).isTrue();
    }

    @Test
    public void returnsTrueIfRefinedBlockOnly() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        ar.expression(new TestExpression());
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.FULL);
        assertThat(broadcaster.find(AtRuleBlock.class).isPresent()).isTrue();
    }

    @Test
    public void errorsIfMissingExpression() {
        AtRule ar = new AtRule(1, 1, "media", null, new RawSyntax(2, 2, ".class{color:red}"), refiner);
        exception.expect(ParserException.class);
        exception.expectMessage(Message.MEDIA_EXPR.message());
        strategy.refine(ar, broadcaster, refiner);
    }

    @Test
    public void errorsIfDidntFindMediaList() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, ""), new RawSyntax(2, 2, ""), refiner);
        exception.expect(ParserException.class);
        exception.expectMessage(Message.DIDNT_FIND_MEDIA_LIST.message());
        strategy.refine(ar, broadcaster, refiner);
    }

    @Test
    public void errirsIfUnparsableRemainderInExpression() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all$"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        exception.expect(ParserException.class);
        exception.expectMessage("Unable to parse");
        strategy.refine(ar, broadcaster, refiner);
    }

    @Test
    public void errorsIfMissingBlock() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), null, refiner);
        exception.expect(ParserException.class);
        exception.expectMessage(Message.MEDIA_BLOCK.message());
        strategy.refine(ar, broadcaster, refiner);
    }

    @Test
    public void errorsIfUnparsableRemainderInBlock() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}$"), refiner);
        exception.expect(ParserException.class);
        exception.expectMessage("Unable to parse");
        strategy.refine(ar, broadcaster, refiner);
    }

    @Test
    public void setsTheExpression() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.FULL);
        assertThat(ar.expression().isPresent()).isTrue();
    }

    @Test
    public void broadcastsTheExpression() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.FULL);
        assertThat(broadcaster.find(MediaQueryList.class).isPresent()).isTrue();
    }

    @Test
    public void setsTheBlock() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.FULL);
        assertThat(ar.block().isPresent()).isTrue();
    }

    @Test
    public void broadcastsTheBlock() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, ".class{color:red}"), refiner);
        assertThat(strategy.refine(ar, broadcaster, refiner)).isSameAs(Refinement.FULL);
        assertThat(broadcaster.find(com.salesforce.omakase.ast.Rule.class).isPresent()).isTrue();
    }

    private static final class TestExpression  extends AbstractAtRuleMember implements AtRuleExpression {
        @Override
        public void write(StyleWriter writer, StyleAppendable appendable) throws IOException {}

        @Override
        public TestExpression copy() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestBlock extends AbstractAtRuleMember implements AtRuleBlock {
        @Override
        public void write(StyleWriter writer, StyleAppendable appendable) throws IOException {}

        @Override
        public Iterator<Statement> iterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestBlock copy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SyntaxCollection<StatementIterable, Statement> statements() {
            throw new UnsupportedOperationException();
        }
    }
}
