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

package declaration;

import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.Rule;
import com.salesforce.omakase.ast.Status;
import com.salesforce.omakase.ast.atrule.AtRule;
import com.salesforce.omakase.ast.atrule.GenericAtRuleBlock;
import com.salesforce.omakase.ast.atrule.GenericAtRuleExpression;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.declaration.KeywordValue;
import com.salesforce.omakase.ast.declaration.NumericalValue;
import com.salesforce.omakase.ast.declaration.OperatorType;
import com.salesforce.omakase.ast.declaration.PropertyName;
import com.salesforce.omakase.ast.declaration.PropertyValue;
import com.salesforce.omakase.ast.selector.ClassSelector;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.data.Keyword;
import com.salesforce.omakase.data.Prefix;
import com.salesforce.omakase.data.Property;
import com.salesforce.omakase.parser.refiner.MasterRefiner;
import com.salesforce.omakase.test.StatusChangingBroadcaster;
import com.salesforce.omakase.util.Values;
import com.salesforce.omakase.writer.StyleWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

/** Unit tests for {@link Declaration}. */
@SuppressWarnings("JavaDoc")
public class DeclarationTest {
    @org.junit.Rule public final ExpectedException exception = ExpectedException.none();

    private RawSyntax rawName;
    private RawSyntax rawValue;
    private Declaration fromRaw;

    @Before
    public void setup() {
        rawName = new RawSyntax(2, 3, "display");
        rawValue = new RawSyntax(2, 5, "none");
        fromRaw = new Declaration(rawName, rawValue, new MasterRefiner(new StatusChangingBroadcaster()));
    }

    @Test
    public void rawValues() {
        assertThat(fromRaw.rawPropertyName().get()).isSameAs(rawName);
        assertThat(fromRaw.rawPropertyValue().get()).isSameAs(rawValue);
        assertThat(fromRaw.line()).isEqualTo(rawName.line());
        assertThat(fromRaw.column()).isEqualTo(rawName.column());
    }

    @Test
    public void setPropertyName() {
        Declaration d = new Declaration(Property.MARGIN, NumericalValue.of(5, "px"));
        d.propertyName(PropertyName.of(Property.PADDING));
        assertThat(d.propertyName().name()).isEqualTo("padding");
    }

    @Test
    public void setPropertyNameUsingShorthand() {
        Declaration d = new Declaration(Property.MARGIN, NumericalValue.of(5, "px"));
        d.propertyName(Property.PADDING);
        assertThat(d.propertyName().name()).isEqualTo("padding");
    }

    @Test
    public void setPropertyNameUsingString() {
        Declaration d = new Declaration(Property.ORDER, NumericalValue.of(5));
        d.propertyName("-webkit-order");
        assertThat(d.propertyName().name()).isEqualTo("-webkit-order");
        assertThat(d.propertyName().prefix().get()).isEqualTo(Prefix.WEBKIT);
    }

    @Test
    public void getPropertyNameWhenUnrefined() {
        assertThat(fromRaw.propertyName().name()).isEqualTo("display");
    }

    @Test
    public void getPropertyNameWhenRefined() {
        assertThat(fromRaw.propertyName().name()).isEqualTo("display");
        assertThat(fromRaw.propertyName().name()).isEqualTo("display");
    }

    @Test
    public void getName() {
        assertThat(fromRaw.propertyName().name()).isEqualTo(fromRaw.name());
    }

    @Test
    public void setPropertyValue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        PropertyValue newValue = PropertyValue.of(KeywordValue.of(Keyword.BLOCK));
        d.propertyValue(newValue);
        assertThat(d.propertyValue()).isSameAs(newValue);
    }

    @Test
    public void setPropertyValueShorthand() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.propertyValue(KeywordValue.of(Keyword.BLOCK));
        assertThat(Values.asKeyword(d.propertyValue()).isPresent()).isTrue();
    }

    @Test
    public void newPropertyValueIsBroadcasted() {
        Rule rule = new Rule(1, 1, new StatusChangingBroadcaster());
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        PropertyValue newValue = PropertyValue.of(KeywordValue.of(Keyword.BLOCK));
        d.propertyValue(newValue);

        assertThat(newValue.status()).isSameAs(Status.UNBROADCASTED);
        rule.declarations().append(d);
        assertThat(newValue.status()).isNotSameAs(Status.UNBROADCASTED);
    }

    @Test
    public void changedPropertyValueIsBroadcasted() {
        Rule rule = new Rule(1, 1, new StatusChangingBroadcaster());
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        rule.declarations().append(d);

        PropertyValue newValue = PropertyValue.of(KeywordValue.of(Keyword.BLOCK));
        assertThat(newValue.status()).isSameAs(Status.UNBROADCASTED);

        d.propertyValue(newValue);
        assertThat(newValue.status()).isSameAs(Status.PROCESSED);
    }

    @Test
    public void setPropertyValueDoesntBroadcastAlreadyBroadcasted() {
        StatusChangingBroadcaster broadcaster = new StatusChangingBroadcaster();
        Rule rule = new Rule(1, 1, broadcaster);
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.status(Status.PROCESSED);

        PropertyValue newValue = PropertyValue.of(KeywordValue.of(Keyword.BLOCK));
        newValue.status(Status.PROCESSED);
        d.propertyValue(newValue);

        rule.declarations().append(d);
        assertThat(broadcaster.all).isEmpty();
    }

    @Test
    public void setPropertyValueAssignsParent() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        PropertyValue newValue = PropertyValue.of(KeywordValue.of(Keyword.BLOCK));
        d.propertyValue(newValue);
        assertThat(d.propertyValue().declaration()).isSameAs(d);
    }

    @Test
    public void setPropertyValueRemovesParentFromOldPropertyValue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        PropertyValue oldValue = d.propertyValue();

        PropertyValue newValue = PropertyValue.of(KeywordValue.of(Keyword.BLOCK));
        d.propertyValue(newValue);

        assertThat(oldValue.declaration()).isNull();
        assertThat(oldValue.status() == Status.NEVER_EMIT);
    }

    @Test
    public void propagatebroadcastBroadcastsPropertyValue() {
        PropertyValue pv = PropertyValue.of(KeywordValue.of(Keyword.NONE));
        Declaration d = new Declaration(Property.DISPLAY, pv);

        assertThat(pv.status()).isSameAs(Status.UNBROADCASTED);
        d.propagateBroadcast(new StatusChangingBroadcaster());
        assertThat(pv.status()).isNotSameAs(Status.UNBROADCASTED);
    }

    @Test
    public void getPropertyValueWhenUnrefined() {
        // should automatically refine to the property value)
        assertThat(fromRaw.propertyValue()).isNotNull();
    }

    @Test
    public void getPropertyValueWhenRefined() {
        // automatic refinement should not occur since we are already refined, hence should be the same object
        PropertyValue propertyValue = fromRaw.propertyValue();
        assertThat(fromRaw.propertyValue()).isSameAs(propertyValue);
    }

    @Test
    public void isPropertyWithAnotherPropertyNameTrue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isProperty(PropertyName.of(Property.DISPLAY))).isTrue();
    }

    @Test
    public void isPropertyWithAnotherPropertyNameFalse() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isProperty(PropertyName.of(Property.COLOR))).isFalse();
    }

    @Test
    public void isPropertyStringTrue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isProperty("display")).isTrue();
    }

    @Test
    public void isPropertyStringFalse() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isProperty("color")).isFalse();
    }

    @Test
    public void isPropertyTrue() {
        Declaration d = new Declaration(Property.DISPLAY, PropertyValue.of(KeywordValue.of(Keyword.NONE)));
        assertThat(d.isProperty(PropertyName.of(Property.DISPLAY))).isTrue();
    }

    @Test
    public void isPropertyFalse() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isProperty(Property.COLOR)).isFalse();
    }

    @Test
    public void isPropertyIgnorePrefixTrue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.MOZ);
        assertThat(d.isPropertyIgnorePrefix(Property.DISPLAY)).isTrue();
    }

    @Test
    public void isPropertyIgnorePrefixFalse() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.MOZ);
        assertThat(d.isPropertyIgnorePrefix(Property.MARGIN)).isFalse();
    }

    @Test
    public void isPropertyIgnorePrefixForPNTrue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.MOZ);
        assertThat(d.isPropertyIgnorePrefix(PropertyName.of("display"))).isTrue();
    }

    @Test
    public void isPropertyIgnorePrefixForPNFalse() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.MOZ);
        assertThat(d.isPropertyIgnorePrefix(PropertyName.of("margin"))).isFalse();
    }

    @Test
    public void isPropertyIgnorePrefixForStringTrue() {
        Declaration d = new Declaration(Property.ORDER, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.WEBKIT);
        assertThat(d.isPropertyIgnorePrefix("order")).isTrue();
    }

    @Test
    public void isPropertyIgnorePrefixForStringFalse() {
        Declaration d = new Declaration(Property.ORDER, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.WEBKIT);
        assertThat(d.isPropertyIgnorePrefix("ordinal")).isFalse();
    }

    @Test
    public void isPrefixedTrue() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        d.propertyName().prefix(Prefix.MOZ);
        assertThat(d.isPrefixed()).isTrue();
    }

    @Test
    public void isPrefixedFalse() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isPrefixed()).isFalse();
    }

    @Test
    public void isRefinedTrue() {
        fromRaw.refine();
        assertThat(fromRaw.isRefined()).isTrue();
    }

    @Test
    public void isRefinedFalse() {
        assertThat(fromRaw.isRefined()).isFalse();
    }

    @Test
    public void isRefinedTrueForDynamicallyCreatedUnit() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        assertThat(d.isRefined()).isTrue();
    }

    @Test
    public void refine() {
        fromRaw.refine();
        assertThat(fromRaw.propertyName()).isNotNull();
        assertThat(fromRaw.propertyValue()).isNotNull();
    }

    @Test
    public void writeVerboseRefined() throws IOException {
        PropertyValue terms = PropertyValue.ofTerms(OperatorType.SPACE, NumericalValue.of(1, "px"), NumericalValue.of(2, "px"));
        Declaration d = new Declaration(Property.MARGIN, terms);
        assertThat(StyleWriter.verbose().writeSingle(d)).isEqualTo("margin: 1px 2px");
    }

    @Test
    public void writeInlineRefined() throws IOException {
        PropertyValue terms = PropertyValue.ofTerms(OperatorType.SPACE, NumericalValue.of(1, "px"), NumericalValue.of(2, "px"));
        Declaration d = new Declaration(Property.MARGIN, terms);
        assertThat(StyleWriter.inline().writeSingle(d)).isEqualTo("margin:1px 2px");
    }

    @Test
    public void writeCompressedRefined() throws IOException {
        PropertyValue terms = PropertyValue.ofTerms(OperatorType.SPACE, NumericalValue.of(1, "px"), NumericalValue.of(2, "px"));
        Declaration d = new Declaration(Property.MARGIN, terms);
        assertThat(StyleWriter.compressed().writeSingle(d)).isEqualTo("margin:1px 2px");
    }

    @Test
    public void writeVerboseUnrefined() throws IOException {
        RawSyntax name = new RawSyntax(2, 3, "border");
        RawSyntax value = new RawSyntax(2, 5, "1px solid red");
        Declaration d = new Declaration(name, value, new MasterRefiner(new StatusChangingBroadcaster()));

        assertThat(StyleWriter.verbose().writeSingle(d)).isEqualTo("border: 1px solid red");
    }

    @Test
    public void writeInlineUnrefined() throws IOException {
        RawSyntax name = new RawSyntax(2, 3, "border");
        RawSyntax value = new RawSyntax(2, 5, "1px solid red");
        Declaration d = new Declaration(name, value, new MasterRefiner(new StatusChangingBroadcaster()));

        assertThat(StyleWriter.inline().writeSingle(d)).isEqualTo("border:1px solid red");
    }

    @Test
    public void writeCompressedUnrefined() throws IOException {
        RawSyntax name = new RawSyntax(2, 3, "border");
        RawSyntax value = new RawSyntax(2, 5, "1px solid red");
        Declaration d = new Declaration(name, value, new MasterRefiner(new StatusChangingBroadcaster()));

        assertThat(StyleWriter.compressed().writeSingle(d)).isEqualTo("border:1px solid red");
    }

    @Test
    public void isWritableWhenAttached() {
        Declaration d = new Declaration(Property.DISPLAY, KeywordValue.of(Keyword.NONE));
        Rule rule = new Rule();
        rule.declarations().append(d);
        assertThat(d.isWritable()).isTrue();
    }

    @Test
    public void isWritableWhenUnrefinedAndAttached() {
        RawSyntax name = new RawSyntax(2, 3, "border");
        RawSyntax value = new RawSyntax(2, 5, "1px solid red");
        Declaration d = new Declaration(name, value, new MasterRefiner(new StatusChangingBroadcaster()));
        Rule rule = new Rule();
        rule.declarations().append(d);
        assertThat(d.isWritable()).isTrue();
    }

    @Test
    public void isNotWritableWhenPropertyValueNotWritable() {
        Declaration d = new Declaration(Property.DISPLAY, new PropertyValue());
        Rule rule = new Rule();
        rule.declarations().append(d);
        assertThat(d.isWritable()).isFalse();
    }

    @Test
    public void copy() {
        Declaration d = new Declaration(Property.MARGIN, NumericalValue.of(5, "px"));
        d.comments(Lists.newArrayList("test"));

        Declaration copy = d.copy();
        assertThat(copy.isProperty(Property.MARGIN));
        assertThat(copy.propertyValue()).isInstanceOf(PropertyValue.class);
        assertThat(copy.comments()).hasSameSizeAs(d.comments());
    }

    @Test
    public void copyUnrefined() {
        Declaration copy = fromRaw.copy();
        assertThat(copy.isProperty(Property.DISPLAY));
        assertThat(copy.propertyValue()).isInstanceOf(PropertyValue.class);
    }

    @Test
    public void testParentAtRulePresent() {
        Declaration d = new Declaration(Property.COLOR, KeywordValue.of(Keyword.RED));
        Rule rule = new Rule(1, 1, new StatusChangingBroadcaster());
        rule.selectors().append(new Selector(new ClassSelector("test")));
        rule.declarations().append(d);

        GenericAtRuleBlock block = new GenericAtRuleBlock();
        block.statements().append(rule);
        AtRule ar = new AtRule("media", new GenericAtRuleExpression(1, 1, "all"), block);

        assertThat(d.parentAtRule().get()).isSameAs(ar);
    }

    @Test
    public void testParentAtRuleAbsent() {
        Declaration d = new Declaration(Property.COLOR, KeywordValue.of(Keyword.RED));
        Rule rule = new Rule(1, 1, new StatusChangingBroadcaster());
        rule.selectors().append(new Selector(new ClassSelector("test")));
        rule.declarations().append(d);

        assertThat(d.parentAtRule().isPresent()).isFalse();
    }

    @Test
    public void testDestroyWithNoMembers() {
        Declaration d = new Declaration(PropertyName.of(Property.MARGIN), new PropertyValue());
        d.destroy();
        assertThat(d.isDestroyed()).isTrue();

    }

    @Test
    public void testDestroyAlsoDestroysInnerTerms() {
        PropertyValue terms = PropertyValue.ofTerms(OperatorType.SPACE, NumericalValue.of(1, "px"), NumericalValue.of(2, "px"));
        Declaration d = new Declaration(Property.MARGIN, terms);

        d.destroy();
        assertThat(d.isDestroyed()).isTrue();
        assertThat(d.propertyValue().members().isEmpty());
    }
}
