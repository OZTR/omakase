/*
 * Copyright (C) 2014 salesforce.com, inc.
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

package com.salesforce.omakase.test.sample.custom.function;

import com.google.common.collect.ImmutableMap;
import com.salesforce.omakase.Omakase;
import com.salesforce.omakase.broadcast.annotation.Subscribable;
import com.salesforce.omakase.plugin.SyntaxPlugin;
import com.salesforce.omakase.plugin.validator.StandardValidation;
import com.salesforce.omakase.test.sample.custom.function.CustomVarRefiner.Mode;
import com.salesforce.omakase.writer.StyleWriter;

import java.io.IOException;
import java.util.Map;

/**
 * An example of using the sample custom function classes.
 * <p/>
 * The custom function represents a variable lookup, using the format <code>custom-var(varName)</code>.
 * <p/>
 * We give the parser an instance of the {@link CustomVarPlugin}. This plugin is a {@link SyntaxPlugin} that registers our {@link
 * CustomVarRefiner}. The refiner handles actually parsing the custom function, and creates {@link CustomVarFunction} AST objects.
 * Because we make the AST object {@link Subscribable}, it can be subcribed to like any other standard AST objects, which our
 * {@link CustomVarCounter} plugin demonstrates.
 * <p/>
 * This sample usage parses a CSS source twice. The first time we just count the number of times the custom function is used, but
 * we don't resolve anything. The second time we resolve and replace the custom function with the substituted values.
 * <p/>
 * Things to try:
 * <p/>
 * <b>1)</b> Change the variable values. <b>2)</b> Have the sample CSS reference an invalid variable. <b>3)</b> Have a variable
 * value result in invalid CSS (e.g., making primary-color too many chars). <b>4)</b> Write another custom plugin that validates
 * Terms, and see how it validates the substituted variable values as well, etc...
 *
 * @author nmcwilliams
 */
@SuppressWarnings("ALL")
public final class SampleUsage {
    // the sample variables
    private static final Map<String, String> VARS = ImmutableMap.<String, String>builder()
        .put("primary-color", "#56ff00")
        .put("font-family", "Arial, 'Helvetica Neue', Helvetica, sans-serif")
        .build();

    public static void main(String[] args) throws IOException {
        // sample CSS input
        String input = "" +
            ".button {\n" +
            "  font-size: 13px;\n" +
            "  background: #f2f2f2;\n" +
            "  color: custom-var(primary-color);\n" +
            "  font-family: custom-var(font-family);\n" +
            "}\n" +
            "\n" +
            ".panel {\n" +
            "  background: custom-var(primary-color)\n" +
            "}";

        System.out.println("Sample Custom Function\n");

        System.out.println("INPUT:\n--------------------");
        System.out.println(input);
        System.out.println();

        // setup the plugins we want
        StyleWriter verbose = StyleWriter.verbose();
        StandardValidation validation = new StandardValidation();
        CustomVarPlugin passthrough = new CustomVarPlugin(Mode.PASSTHROUGH, VARS);
        CustomVarPlugin resolving = new CustomVarPlugin(Mode.RESOLVE, VARS);
        CustomVarCounter counting = new CustomVarCounter();

        // parse without resolving the vars, but count them
        Omakase.source(input)
            .use(verbose)
            .use(validation)
            .use(passthrough)
            .use(counting)
            .process();

        System.out.println("\nOUTPUT (passthrough):\n-------------------------");
        verbose.writeTo(System.out);
        counting.summarize(System.out);

        // this time resolve the vars
        Omakase.source(input)
            .use(verbose)
            .use(validation)
            .use(resolving)
            .process();

        System.out.println("\n\n\nOUTPUT (resolved):\n-------------------------");
        verbose.writeTo(System.out);

        System.out.println("\n\n");
    }
}