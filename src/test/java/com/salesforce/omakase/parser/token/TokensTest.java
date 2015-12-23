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

package com.salesforce.omakase.parser.token;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit test for {@link Tokens}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class TokensTest {
    @Test
    public void testMatches() {
        assertThat(Tokens.ALPHA.matches('a')).isTrue();
        assertThat(Tokens.ALPHA.matches('A')).isTrue();
        assertThat(Tokens.ALPHA.matches('8')).isFalse();
        assertThat(Tokens.DIGIT.matches('0')).isTrue();
        assertThat(Tokens.DIGIT.matches('9')).isTrue();
        assertThat(Tokens.DIGIT.matches('_')).isFalse();
        assertThat(Tokens.NMCHAR.matches('9')).isTrue();
        assertThat(Tokens.NMCHAR.matches('a')).isTrue();
        assertThat(Tokens.NMCHAR.matches('$')).isFalse();
    }

    @Test
    public void doesntMatchNull() {
        for (Tokens tokens : Tokens.values()) {
            assertThat(tokens.matches('\u0000')).isFalse();
        }
    }
}
