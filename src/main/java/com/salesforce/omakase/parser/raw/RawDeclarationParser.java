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

import com.google.common.base.Optional;
import com.salesforce.omakase.Message;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.broadcast.Broadcaster;
import com.salesforce.omakase.parser.AbstractParser;
import com.salesforce.omakase.parser.Source;
import com.salesforce.omakase.parser.refiner.MasterRefiner;
import com.salesforce.omakase.parser.token.Token;

/**
 * Parses a {@link Declaration}.
 *
 * @author nmcwilliams
 * @see Declaration
 */
public final class RawDeclarationParser extends AbstractParser {
    @Override
    public boolean parse(Source source, Broadcaster broadcaster, MasterRefiner refiner) {
        source.collectComments();

        // grab our current position before parsing anything
        int line = source.originalLine();
        int column = source.originalColumn();

        Optional<Token> specialToken = refiner.tokenFactory().specialDeclarationBegin();
        Optional<Character> special = Optional.absent();

        if (specialToken.isPresent()) {
            special = source.optional(specialToken.get());
        }

        // read the property name
        Optional<String> ident = source.readIdent();
        if (!ident.isPresent()) return false;

        String content = special.isPresent() ? special.get() + ident.get() : ident.get();
        RawSyntax property = new RawSyntax(line, column, content.trim());

        // read colon
        source.skipWhitepace();
        source.expect(refiner.tokenFactory().propertyNameEnd(), Message.MISSING_COLON);
        source.skipWhitepace();

        //read the property value
        line = source.originalLine();
        column = source.originalColumn();
        content = source.until(refiner.tokenFactory().declarationEnd());
        RawSyntax value = new RawSyntax(line, column, content.trim());

        // create the new declaration and associate comments
        Declaration declaration = new Declaration(property, value, refiner);
        declaration.comments(source.flushComments());

        // notifier listeners of the new declaration
        broadcaster.broadcast(declaration);
        return true;
    }
}
