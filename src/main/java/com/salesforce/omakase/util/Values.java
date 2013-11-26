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

package com.salesforce.omakase.util;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.declaration.*;

import java.util.Iterator;
import java.util.List;

/**
 * Utilities for working with {@link PropertyValue}s and {@link Term}s.
 * <p/>
 * This allows you to extract specific {@link Term} instances from a given {@link PropertyValue} if that {@link Term} is the only
 * member within the list.
 * <p/>
 * Examples:
 * <pre>
 * {@code Optional<HexColorValue> color = Value.asHexColor(declaration.propertyValue())}
 * {@code Optional<KeywordValue> keyword = Value.asKeyword(declaration.propertyValue())}
 * {@code Optional<NumericalValue> number = Value.asNumerical(declaration.propertyValue())}
 * </pre>
 *
 * @author nmcwilliams
 */
public final class Values {
    /** do not construct */
    private Values() {}

    /**
     * Gets the single {@link HexColorValue} within the given {@link PropertyValue}. The returned {@link Optional} will only be
     * present if the given property value contains only one {@link Term} which is an instance of a {@link HexColorValue}.
     * <p/>
     * This allows you to work with {@link HexColorValue} specific methods when getting a {@link Declaration}'s property value.
     * <p/>
     * Example:
     * <pre>
     * {@code HexColorValue color = Value.asHexColor(declaration.getPropertyValue())}
     * </pre>
     *
     * @param value
     *     The value.
     *
     * @return The hex color value, or {@link Optional#absent()} if the {@link PropertyValue} doesn't match the conditions as
     *         stated above.
     */
    public static Optional<HexColorValue> asHexColor(PropertyValue value) {
        return as(HexColorValue.class, value);
    }

    /**
     * Gets the single {@link KeywordValue} within the given {@link PropertyValue}. The returned {@link Optional} will only be
     * present if the given property value contains only one {@link Term} which is an instance of a {@link KeywordValue}.
     * <p/>
     * This allows you to work with {@link KeywordValue} specific methods when getting a {@link Declaration}'s property value.
     * <p/>
     * Example:
     * <pre>
     * {@code KeywordValue keyword = Value.asKeyword(declaration.getPropertyValue())}
     * </pre>
     *
     * @param value
     *     The value.
     *
     * @return The keyword value, or {@link Optional#absent()} if the {@link PropertyValue} doesn't match the conditions as stated
     *         above.
     */
    public static Optional<KeywordValue> asKeyword(PropertyValue value) {
        return as(KeywordValue.class, value);
    }

    /**
     * Gets the single {@link NumericalValue} within the given {@link PropertyValue}. The returned {@link Optional} will only be
     * present if the given property value contains only one {@link Term} which is an instance of a {@link NumericalValue}.
     * <p/>
     * This allows you to work with {@link NumericalValue} specific methods when getting a {@link Declaration}'s property value.
     * <p/>
     * Example:
     * <pre>
     * {@code NumericalValue number = Value.asNumerical(declaration.getPropertyValue())}
     * </pre>
     *
     * @param value
     *     The value.
     *
     * @return The numerical value, or {@link Optional#absent()} if the {@link PropertyValue} doesn't match the conditions as
     *         stated above.
     */
    public static Optional<NumericalValue> asNumerical(PropertyValue value) {
        return as(NumericalValue.class, value);
    }

    /**
     * Gets the single {@link StringValue} within the given {@link PropertyValue}. The returned {@link Optional} will only be
     * present if the given property value contains only one {@link Term} which is an instance of a {@link StringValue}.
     * <p/>
     * This allows you to work with {@link StringValue} specific methods when getting a {@link Declaration}'s property value.
     * <p/>
     * Example:
     * <pre>
     * {@code StringValue string = Value.asString(declaration.getPropertyValue())}
     * </pre>
     *
     * @param value
     *     The value.
     *
     * @return The string value, or {@link Optional#absent()} if the {@link PropertyValue} doesn't match the conditions as stated
     *         above.
     */
    public static Optional<StringValue> asString(PropertyValue value) {
        return as(StringValue.class, value);
    }

    /**
     * Helper method to convert or extract the more narrowly-typed {@link Term} instance.
     *
     * @param <T>
     *     The type of {@link Term}.
     * @param klass
     *     Class of the {@link Term}.
     * @param value
     *     The {@link PropertyValue} that is or contains the {@link Term}.
     *
     * @return the properly-typed instance, or {@link Optional#absent()} if it doesn't match.
     */
    public static <T extends Term> Optional<T> as(Class<T> klass, PropertyValue value) {
        ImmutableList<Term> terms = value.terms();
        if (terms.size() == 1) {
            Term term = terms.get(0);
            if (klass.isAssignableFrom(term.getClass())) return Optional.of(klass.cast(term));
        }
        return Optional.absent();
    }

    /**
     * Filters the terms in the given {@link PropertyValue} to only the ones of the given class type.
     * <p/>
     * Example:
     * <pre>
     * {@code Iterable<FunctionValue> functions = Values.filter(FunctionValue.class, declaration.propertyValue();}
     * </pre>
     *
     * @param klass
     *     Filter to terms of this class.
     * @param value
     *     The {@link PropertyValue} to filter.
     * @param <T>
     *     Filters to terms of this type.
     *
     * @return The filtered results.
     */
    public static <T extends Term> Iterable<T> filter(Class<T> klass, PropertyValue value) {
        List<T> filtered = Lists.newArrayList();
        for (PropertyValueMember member : value.members()) {
            if (klass.isAssignableFrom(member.getClass())) filtered.add(klass.cast(member));
        }
        return filtered;
    }

    /**
     * Splits the {@link PropertyValue} at the given operator.
     * <p/>
     * For example, given a {@link PropertyValue} with the following:
     * <pre>{Term}{SPACE}{Term}{SLASH}{Term}{SPACE}{Term}</pre>
     * <p/>
     * Calling this method with {@link OperatorType#SLASH} would return a list with two new {@link PropertyValue} instances. The
     * first one would contain the first two terms and space operator, and the second one would contain the last two terms and
     * space operator.
     *
     * @param operatorType
     *     Split on this {@link OperatorType}.
     * @param value
     *     The {@link PropertyValue} to split.
     *
     * @return The list of new {@link PropertyValue}s split from the original.
     *
     * @see #join(OperatorType, Iterable)
     */
    public static List<PropertyValue> split(OperatorType operatorType, PropertyValue value) {
        PropertyValue current = new PropertyValue();
        List<PropertyValue> split = Lists.newArrayList(current);

        for (PropertyValueMember member : value.members()) {
            if (member instanceof Operator && ((Operator)member).type() == operatorType) {
                current = new PropertyValue();
                split.add(current);
                continue;
            }
            current.append(member);
        }

        return split;
    }

    /**
     * Joins multiple {@link PropertyValue}s together with the given {@link OperatorType}.
     *
     * @param operatorType
     *     Join using this {@link OperatorType}.
     * @param toJoin
     *     The list of {@link PropertyValue}s to join.
     *
     * @return The new {@link PropertyValue} instance.
     */
    public static PropertyValue join(OperatorType operatorType, Iterable<PropertyValue> toJoin) {
        PropertyValue joined = new PropertyValue();

        for (Iterator<PropertyValue> it = toJoin.iterator(); it.hasNext(); ) {
            joined.members().appendAll(it.next().members());
            if (it.hasNext()) joined.append(operatorType);
        }

        return joined;
    }
}