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

package com.salesforce.omakase.ast.declaration;

import com.google.common.collect.Lists;
import com.salesforce.omakase.data.Prefix;
import com.salesforce.omakase.data.Property;
import com.salesforce.omakase.writer.StyleWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

/** Unit tests for {@link PropertyName}. */
@SuppressWarnings("JavaDoc")
public class PropertyNameTest {
    private static final Prefix PREFIX = Prefix.WEBKIT;
    private static final String NAME = "border-radius";
    private static final String STARHACK_NAME = "*color";

    @Rule public final ExpectedException exception = ExpectedException.none();

    private PropertyName prefixed;
    private PropertyName unprefixed;

    @Before
    public void setup() {
        prefixed = PropertyName.of(PREFIX + NAME);
        unprefixed = PropertyName.of(NAME);
    }

    @Test
    public void nameWithPrefix() {
        assertThat(prefixed.name()).isEqualTo(PREFIX + NAME);
    }

    @Test
    public void nameWithoutPrefix() {
        assertThat(unprefixed.name()).isEqualTo(NAME);
    }

    @Test
    public void unprefixedNameWhenPrefixIsPresent() {
        assertThat(prefixed.unprefixed()).isEqualTo(NAME);
    }

    @Test
    public void unprefixedNameWhenPrefixIsAbsent() {
        assertThat(unprefixed.unprefixed()).isEqualTo(NAME);
    }

    @Test
    public void isPrefixedTrue() {
        assertThat(prefixed.isPrefixed()).isTrue();
    }

    @Test
    public void isPrefixedFalse() {
        assertThat(unprefixed.isPrefixed()).isFalse();
    }

    @Test
    public void prefixPresent() {
        assertThat(prefixed.prefix().get()).isSameAs(PREFIX);
    }

    @Test
    public void prefixAbsent() {
        assertThat(unprefixed.prefix().isPresent()).isFalse();
    }

    @Test
    public void setsPrefixWhenPreviouslyUnset() {
        unprefixed.prefix(Prefix.MOZ);
        assertThat(unprefixed.prefix().get()).isSameAs(Prefix.MOZ);
    }

    @Test
    public void removePrefix() {
        prefixed.removePrefix();
        assertThat(prefixed.prefix().isPresent()).isFalse();
    }

    @Test
    public void hasPrefixTrue() {
        PropertyName pn = PropertyName.of(Property.DISPLAY);
        pn.prefix(Prefix.MOZ);
        assertThat(pn.hasPrefix(Prefix.MOZ)).isTrue();
    }

    @Test
    public void hasPrefixFalse() {
        PropertyName pn = PropertyName.of(Property.DISPLAY);
        pn.prefix(Prefix.MOZ);
        assertThat(pn.hasPrefix(Prefix.WEBKIT)).isFalse();
    }

    @Test
    public void hasPrefixFalseWhenPrefixNotPresent() {
        assertThat(unprefixed.hasPrefix(Prefix.WEBKIT)).isFalse();
    }

    @Test
    public void asPropertyAbsentWhenPrefixed() {
        assertThat(prefixed.asProperty().isPresent()).isFalse();
    }

    @Test
    public void asPropertyUnknownProperty() {
        assertThat(PropertyName.of("blah").asProperty().isPresent()).isFalse();
    }

    @Test
    public void asPropertyKnownProperty() {
        assertThat(PropertyName.of(Property.DISPLAY).asProperty().get()).isSameAs(Property.DISPLAY);
    }

    @Test
    public void asPropertyIgnorePrefixHasPrefix() {
        assertThat(prefixed.asPropertyIgnorePrefix().get()).isSameAs(Property.BORDER_RADIUS);
    }

    @Test
    public void asPropertyIgnorePrefixDoesntHavePrefix() {
        assertThat(unprefixed.asPropertyIgnorePrefix().get()).isSameAs(Property.BORDER_RADIUS);
    }

    @Test
    public void asPropertyIgnorPrefixAbsentWhenUnknown() {
        assertThat(PropertyName.of("-webkit-blah").asPropertyIgnorePrefix().isPresent()).isFalse();
    }

    @Test
    public void matchesIgnorePrefixTrueWhenPrefixed() {
        assertThat(prefixed.matchesIgnorePrefix(Property.BORDER_RADIUS)).isTrue();
    }

    @Test
    public void matchesIgnorePrefixTrueWhenNotPrefixed() {
        assertThat(unprefixed.matchesIgnorePrefix(Property.BORDER_RADIUS)).isTrue();
    }

    @Test
    public void matchesIgnorePrefixFalseWhenPrefixed() {
        assertThat(prefixed.matchesIgnorePrefix(Property.BORDER)).isFalse();
    }

    @Test
    public void matchesIgnorePrefixFalseWhenNotPrefixed() {
        assertThat(unprefixed.matchesIgnorePrefix(Property.BORDER)).isFalse();
    }

    @Test
    public void matchesPropertyNameIgnorePrefix() {
        PropertyName pn1 = PropertyName.of("-webkit-border-radius");
        PropertyName pn2 = PropertyName.of("-moz-border-radius");
        assertThat(pn1.matchesIgnorePrefix(pn2)).isTrue();
    }

    @Test
    public void matchesIgnorePrefixTrueWhenPrefixedString() {
        assertThat(prefixed.matchesIgnorePrefix("border-radius")).isTrue();
    }

    @Test
    public void matchesIgnorePrefixTrueWhenNotPrefixedString() {
        assertThat(unprefixed.matchesIgnorePrefix("border-radius")).isTrue();
    }

    @Test
    public void matchesIgnorePrefixFalseWhenPrefixedString() {
        assertThat(prefixed.matchesIgnorePrefix("border")).isFalse();
    }

    @Test
    public void matchesIgnorePrefixFalseWhenNotPrefixedString() {
        assertThat(unprefixed.matchesIgnorePrefix("border")).isFalse();
    }

    @Test
    public void writeForPropertyWithPrefix() throws IOException {
        StyleWriter writer = StyleWriter.compressed();
        assertThat(writer.writeSingle(prefixed)).isEqualTo(PREFIX + NAME);
    }

    @Test
    public void writeForPropertyWithoutPrefix() throws IOException {
        StyleWriter writer = StyleWriter.compressed();
        assertThat(writer.writeSingle(unprefixed)).isEqualTo(NAME);
    }

    @Test
    public void constructorMethodStringNameOnly() {
        assertThat(PropertyName.of("color").name()).isEqualTo("color");
    }

    @Test
    public void constructorMethodStringAndPosition() {
        assertThat(PropertyName.of(5, 5, "color").line()).isEqualTo(5);
    }

    @Test
    public void constructorMethodPropertyOnly() {
        assertThat(PropertyName.of(Property.COLOR).name()).isEqualTo(Property.COLOR.toString());
    }

    @Test
    public void matchesSameInstance() {
        assertThat(unprefixed.matches(unprefixed)).isTrue();
    }

    @Test
    public void matchesAnotherPropertyNameWithSameName() {
        assertThat(unprefixed.matches(PropertyName.of(NAME))).isTrue();
    }

    @Test
    public void matchesPropertyWithSameName() {
        PropertyName name = PropertyName.of("display");
        assertThat(name.matches(Property.DISPLAY)).isTrue();
    }

    @Test
    public void matchesStringWithSameName() {
        PropertyName name = PropertyName.of("display");
        assertThat(name.matches("display")).isTrue();
    }

    @Test
    public void doesNotMatchPropertyNameWithDifferentName() {
        assertThat(unprefixed.matches(PropertyName.of("zyx"))).isFalse();
    }

    @Test
    public void doesNotMatchPropertyWithDifferentName() {
        PropertyName name = PropertyName.of("display");
        assertThat(name.matches(Property.COLOR)).isFalse();
    }

    @Test
    public void doesNotMatchStringWithDifferentName() {
        PropertyName name = PropertyName.of("display");
        assertThat(name.matches("color")).isFalse();
    }

    @Test
    public void starHackIsTrue() {
        PropertyName name = PropertyName.of(STARHACK_NAME);
        assertThat(name.hasStarHack()).isTrue();
    }

    @Test
    public void starHackIsStrippedFromProperty() {
        PropertyName name = PropertyName.of(STARHACK_NAME);
        assertThat(name.matches("color")).isTrue();
    }

    @Test
    public void starHackIsWrittenWithStar() throws IOException {
        PropertyName name = PropertyName.of(STARHACK_NAME);
        assertThat(StyleWriter.compressed().writeSingle(name)).isEqualTo(STARHACK_NAME);
    }

    @Test
    public void starHackIsFalse() {
        PropertyName name = PropertyName.of("color");
        assertThat(name.hasStarHack()).isFalse();
    }

    @Test
    public void copyTest() {
        PropertyName name = PropertyName.of("*-webkit-border-radius");
        assertThat(name.isPrefixed()).isTrue();
        assertThat(name.hasStarHack()).isTrue();
        name.comments(Lists.newArrayList("test"));

        PropertyName copy = name.copy();
        assertThat(copy.unprefixed()).isEqualTo(name.unprefixed());
        assertThat(copy.prefix().get()).isEqualTo(name.prefix().get());
        assertThat(copy.hasStarHack()).isEqualTo(name.hasStarHack());
        assertThat(copy.comments()).hasSameSizeAs(name.comments());
    }

    @Test
    public void copyFromUnknownProperty() {
        PropertyName name = PropertyName.of("blah");
        assertThat(name.isPrefixed()).isFalse();
        assertThat(name.hasStarHack()).isFalse();
        name.comments(Lists.newArrayList("test"));

        PropertyName copy = name.copy();
        assertThat(copy.unprefixed()).isEqualTo(name.unprefixed());
        assertThat(copy.prefix().isPresent()).isEqualTo(name.prefix().isPresent());
        assertThat(copy.hasStarHack()).isEqualTo(name.hasStarHack());
        assertThat(copy.comments()).hasSameSizeAs(name.comments());
        assertThat(copy.asProperty().isPresent()).isFalse();
    }
}
