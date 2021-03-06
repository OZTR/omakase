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

package com.salesforce.omakase.plugin.misc;

import com.google.common.base.Optional;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.extended.UnquotedIEFilter;
import com.salesforce.omakase.broadcast.QueryableBroadcaster;
import com.salesforce.omakase.parser.refiner.MasterRefiner;
import com.salesforce.omakase.parser.refiner.Refinement;
import com.salesforce.omakase.util.Values;
import org.junit.Before;
import org.junit.Test;

import static com.salesforce.omakase.plugin.misc.UnquotedIEFilterPlugin.REFINER;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link UnquotedIEFilterPlugin}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class UnquotedIEFilterPluginTest {
    private MasterRefiner refiner;
    private QueryableBroadcaster broadcaster;

    @Before
    public void setup() {
        broadcaster = new QueryableBroadcaster();
        refiner = new MasterRefiner(broadcaster).register(REFINER);
    }

    @Test
    public void refineDeclarationNoMatchReturnsFalse() {
        Declaration d = new Declaration(new RawSyntax(2, 3, "display"), new RawSyntax(2, 5, "none"), refiner);
        assertThat(REFINER.refine(d, broadcaster, refiner)).isSameAs(Refinement.NONE);
        assertThat(d.isRefined()).isFalse();
    }

    @Test
    public void refineDeclarationMatches() {
        Declaration d = new Declaration(new RawSyntax(2, 3, "filter"), new RawSyntax(2, 5,
            "progid:DXImageTransform.Microsoft.Shadow(color='#969696', Direction=145, Strength=3)"), refiner);

        assertThat(REFINER.refine(d, broadcaster, refiner)).isSameAs(Refinement.FULL);
        Optional<UnquotedIEFilter> ief = Values.as(UnquotedIEFilter.class, d.propertyValue());
        assertThat(ief.isPresent()).isTrue();

        assertThat(ief.get().line()).isEqualTo(2);
        assertThat(ief.get().column()).isEqualTo(5);
        assertThat(ief.get().content()).isEqualTo("progid:DXImageTransform.Microsoft.Shadow(color='#969696', Direction=145, " +
            "Strength=3)");
    }
}
