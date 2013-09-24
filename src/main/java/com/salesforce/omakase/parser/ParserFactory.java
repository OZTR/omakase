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

package com.salesforce.omakase.parser;

import com.salesforce.omakase.ast.declaration.value.Term;
import com.salesforce.omakase.ast.selector.PseudoElementSelector;
import com.salesforce.omakase.ast.selector.SimpleSelector;
import com.salesforce.omakase.ast.selector.TypeSelector;
import com.salesforce.omakase.parser.declaration.FunctionValueParser;
import com.salesforce.omakase.parser.declaration.HexColorValueParser;
import com.salesforce.omakase.parser.declaration.ImportantParser;
import com.salesforce.omakase.parser.declaration.KeywordValueParser;
import com.salesforce.omakase.parser.declaration.NumericalValueParser;
import com.salesforce.omakase.parser.declaration.StringValueParser;
import com.salesforce.omakase.parser.declaration.TermListParser;
import com.salesforce.omakase.parser.raw.RawAtRuleParser;
import com.salesforce.omakase.parser.raw.RawDeclarationParser;
import com.salesforce.omakase.parser.raw.RawRuleParser;
import com.salesforce.omakase.parser.raw.RawSelectorParser;
import com.salesforce.omakase.parser.raw.SelectorGroupParser;
import com.salesforce.omakase.parser.raw.StylesheetParser;
import com.salesforce.omakase.parser.selector.*;

/**
 * A cache of {@link Parser} instances.
 * <p/>
 * Each {@link Parser} that is used should usually only be created once (enabled by this class). This is tenable due to the fact
 * that {@link Parser}s are not allowed to maintain state.
 *
 * @author nmcwilliams
 */
public final class ParserFactory {
    /** do not construct */
    private ParserFactory() {}

    /* generic parsers */
    private static final RefinableParser stylesheet = new StylesheetParser();
    private static final RefinableParser atRule = new RawAtRuleParser();
    private static final RefinableParser rule = new RawRuleParser();
    private static final RefinableParser rawDeclaration = new RawDeclarationParser();
    private static final RefinableParser selectorGroup = new SelectorGroupParser();
    private static final RefinableParser rawSelector = new RawSelectorParser();

    /* refined selectors */
    private static final Parser complexSelector = new ComplexSelectorParser();
    private static final Parser combinator = new CombinatorParser();
    private static final Parser classSelector = new ClassSelectorParser();
    private static final Parser idSelector = new IdSelectorParser();
    private static final Parser attributeSelector = new AttributeSelectorParser();
    private static final Parser typeSelector = new TypeSelectorParser();
    private static final Parser universalSelector = new UniversalSelectorParser();
    private static final Parser pseudoSelector = new PseudoSelectorParser();

    private static final Parser repeatableSelector = classSelector
        .or(idSelector)
        .or(attributeSelector)
        .or(pseudoSelector);

    private static final Parser typeOrUniversal = typeSelector.or(universalSelector);

    /* declaration values */
    private static final Parser termList = new TermListParser();
    private static final Parser important = new ImportantParser();

    private static final Parser numericalValue = new NumericalValueParser();
    private static final Parser functionValue = new FunctionValueParser();
    private static final Parser keywordValue = new KeywordValueParser();
    private static final Parser hexColorValue = new HexColorValueParser();
    private static final Parser stringValue = new StringValueParser();

    private static final Parser term = numericalValue.or(functionValue).or(keywordValue).or(hexColorValue).or(stringValue);

    /**
     * Gets the {@link StylesheetParser}.
     *
     * @return The parser instance.
     */
    public static RefinableParser stylesheetParser() {
        return stylesheet;
    }

    /**
     * Gets the {@link RawAtRuleParser}.
     *
     * @return The parser instance.
     */
    public static RefinableParser atRuleParser() {
        return atRule;
    }

    /**
     * Gets the {@link RawRuleParser}.
     *
     * @return The parser instance.
     */
    public static RefinableParser ruleParser() {
        return rule;
    }

    /**
     * Gets the {@link SelectorGroupParser}.
     *
     * @return The parser instance.
     */
    public static RefinableParser selectorGroupParser() {
        return selectorGroup;
    }

    /**
     * Gets the {@link RawSelectorParser}.
     *
     * @return The parser instance.
     */
    public static RefinableParser rawSelectorParser() {
        return rawSelector;
    }

    /**
     * Gets the {@link RawDeclarationParser}.
     *
     * @return The parser instance.
     */
    public static RefinableParser rawDeclarationParser() {
        return rawDeclaration;
    }

    /**
     * Gets the {@link ComplexSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser complexSelectorParser() {
        return complexSelector;
    }

    /**
     * Gets the parser to parse {@link SimpleSelector} (excluding type and universal selectors) or a {@link
     * PseudoElementSelector}.
     *
     * @return The parser instance.
     */
    public static Parser repeatableSelector() {
        return repeatableSelector;
    }

    /**
     * Gets the parser to parse a {@link TypeSelector} or a {@link UniversalSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser typeOrUniversaleSelectorParser() {
        return typeOrUniversal;
    }

    /**
     * Gets the {@link CombinatorParser}.
     *
     * @return The parser instance.
     */
    public static Parser combinatorParser() {
        return combinator;
    }

    /**
     * Gets the {@link ClassSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser classSelectorParser() {
        return classSelector;
    }

    /**
     * Gets the {@link IdSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser idSelectorParser() {
        return idSelector;
    }

    /**
     * Gets the {@link AttributeSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser attributeSelectorParser() {
        return attributeSelector;
    }

    /**
     * Gets the {@link TypeSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser typeSelectorParser() {
        return typeSelector;
    }

    /**
     * Gets the {@link UniversalSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser universalSelectorParser() {
        return universalSelector;
    }

    /**
     * Gets the {@link PseudoSelectorParser}.
     *
     * @return The parser instance.
     */
    public static Parser pseudoSelectorParser() {
        return pseudoSelector;
    }

    /**
     * Gets the {@link TermListParser}.
     *
     * @return The parser instance.
     */
    public static Parser termListParser() {
        return termList;
    }

    /**
     * Gets the parser to parse a {@link Term} value.
     *
     * @return The parser instance.
     */
    public static Parser termParser() {
        return term;
    }

    /**
     * Gets the parser to parse a "important!" value.
     *
     * @return The parser instance.
     */
    public static Parser importantParser() {
        return important;
    }
}
