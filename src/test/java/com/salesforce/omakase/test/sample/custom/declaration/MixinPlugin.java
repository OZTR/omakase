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

package com.salesforce.omakase.test.sample.custom.declaration;

import com.google.common.base.Suppliers;
import com.salesforce.omakase.PluginRegistry;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.broadcast.annotation.Rework;
import com.salesforce.omakase.parser.ParserException;
import com.salesforce.omakase.parser.refiner.RefinerRegistry;
import com.salesforce.omakase.plugin.DependentPlugin;
import com.salesforce.omakase.plugin.SyntaxPlugin;

/**
 * This is the actual plugin that gets registered with the parser.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class MixinPlugin implements SyntaxPlugin, DependentPlugin {
    private static final MixinTokenFactory TOKEN_FACTORY = new MixinTokenFactory();

    @Override
    public void dependencies(PluginRegistry registry) {
        // we need our token factory registered in order to work
        registry.requireTokenFactory(MixinTokenFactory.class, Suppliers.ofInstance(TOKEN_FACTORY));
    }

    @Override
    public void registerRefiners(RefinerRegistry registry) {
        // using registerMulti as this refiner handles multiple types of AST objects
        registry.registerMulti(new MixinRefiner());
    }

    /**
     * This handles taking the {@link MixinReference} within a rule and replacing it copies of the mixin's template declarations.
     * Ideally this could have been in the refiner?... but prepending new declarations is not allowed at that point in time...
     */
    @Rework
    public void resolve(MixinReference mixinReference) {
        Declaration placeholder = mixinReference.declaration();
        Mixin mixin = mixinReference.mixin();

        for (Declaration declaration : mixin.declarations()) {
            RawSyntax rawName = declaration.rawPropertyName().get();
            RawSyntax rawProp = declaration.rawPropertyValue().get();

            // replace var refs
            if (rawProp.content().startsWith("$")) {
                String resolved = mixinReference.params().get(rawProp.content().substring(1));
                if (resolved == null) throw new ParserException(declaration, "unknown mixin param ref");
                rawProp = new RawSyntax(rawProp.line(), rawProp.column(), resolved);
            }

            Declaration cloned = new Declaration(rawName, rawProp, null);
            placeholder.prepend(cloned);
        }

        // not strictly necessary as it won't write out anyway, but we're done with it
        placeholder.destroy();
    }
}
