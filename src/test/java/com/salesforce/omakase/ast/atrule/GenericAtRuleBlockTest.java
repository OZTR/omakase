/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.salesforce.omakase.ast.atrule;

import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.Rule;
import com.salesforce.omakase.ast.Statement;
import com.salesforce.omakase.ast.Stylesheet;
import com.salesforce.omakase.ast.collection.StandardSyntaxCollection;
import com.salesforce.omakase.ast.collection.SyntaxCollection;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.declaration.KeywordValue;
import com.salesforce.omakase.ast.declaration.Property;
import com.salesforce.omakase.ast.declaration.TermList;
import com.salesforce.omakase.ast.selector.ClassSelector;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.broadcast.QueryableBroadcaster;
import com.salesforce.omakase.writer.StyleWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link GenericAtRuleBlock}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class GenericAtRuleBlockTest {
    private Stylesheet stylesheet;
    private Statement statement;

    @Before
    public void setup() {
        Rule rule = new Rule();
        rule.selectors().append(new Selector(new ClassSelector("test")));
        Declaration d = new Declaration(Property.DISPLAY, TermList.singleValue(KeywordValue.of("none")));
        rule.declarations().append(d);
        statement = rule;
        stylesheet = new Stylesheet();
    }

    @Test
    public void getStatement() {
        GenericAtRuleBlock block = new GenericAtRuleBlock(stylesheet);
        block.statements().append(statement);
        assertThat(block.statements()).containsExactly(statement);
    }

    @Test
    public void propagatesBroadcast() {
        GenericAtRuleBlock block = new GenericAtRuleBlock(stylesheet, Lists.newArrayList(statement), null);
        QueryableBroadcaster qb = new QueryableBroadcaster();
        block.propagateBroadcast(qb);
        assertThat(qb.find(Statement.class).get()).isSameAs(statement);
    }

    @Test
    public void isWritableWhenHasStatements() {
        GenericAtRuleBlock block = new GenericAtRuleBlock(stylesheet, Lists.newArrayList(statement), null);
        assertThat(block.isWritable()).isTrue();
    }

    @Test
    public void isNotWritableWithoutStatements() {
        SyntaxCollection<Stylesheet, Statement> collection = StandardSyntaxCollection.create(stylesheet);
        GenericAtRuleBlock block = new GenericAtRuleBlock(collection);
        assertThat(block.isWritable()).isFalse();
    }

    @Test
    public void testWrite() throws IOException {
        GenericAtRuleBlock block = new GenericAtRuleBlock(stylesheet, Lists.newArrayList(statement), null);
        assertThat(StyleWriter.compressed().writeSnippet(block)).isEqualTo(".test{display:none}");
    }
}
