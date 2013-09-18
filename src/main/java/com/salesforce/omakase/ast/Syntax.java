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

package com.salesforce.omakase.ast;

import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.ast.selector.SimpleSelector;
import com.salesforce.omakase.broadcaster.Broadcaster;
import com.salesforce.omakase.emitter.Description;
import com.salesforce.omakase.emitter.Subscribable;
import com.salesforce.omakase.emitter.SubscribableRequirement;
import com.salesforce.omakase.writer.Writable;

import java.util.List;

/**
 * A distinct unit of syntax within CSS.
 * <p/>
 * {@link Syntax} objects are used to represent the individual pieces of content of the parsed CSS source, and are the primary
 * objects used to construct the AST (Abstract Syntax Tree). Not all {@link Syntax} objects have content directly associated with
 * them. Some are used to represent the logical grouping of content, such as the {@link Rule}.
 * <p/>
 * Each unit has a particular line and column indicating where it was parsed within the source, except for dynamically created
 * units. You can check {@link #hasSourcePosition()} to see if a unit is dynamically created.
 * <p/>
 * It's important to remember that <em>unrefined</em> Syntax objects, unless validation is performed, may actually contain invalid
 * CSS. Simply refining the syntax unit will verify it's grammatical compliance, which can be coupled with custom validation to
 * ensure correct usage. See {@link Refinable} for more information.
 *
 * @author nmcwilliams
 */
@Subscribable
@Description(value = "parent interface of all subscribable units", broadcasted = SubscribableRequirement.SPECIAL)
public interface Syntax extends Writable {
    /**
     * The line number within the source where this {@link Syntax} unit was parsed.
     *
     * @return The line number.
     */
    int line();

    /**
     * The column number within the source where this {@link Syntax} unit was parsed.
     *
     * @return The column number.
     */
    int column();

    /**
     * Gets whether this unit has a source location specified.
     * <p/>
     * This will be true for units within the original parsed source and false for dynamically created units.
     *
     * @return True if this unit has a source location specified.
     */
    boolean hasSourcePosition();

    /**
     * Adds the given comments to this unit.
     * <p/>
     * Note that in the case of {@link Selector}s, it is preferred to add comments to the {@link Selector} object itself instead
     * of the individual {@link SimpleSelector}s inside of it. Likewise, it is preferred to add a comment to the {@link
     * Declaration} itself instead of the property name or value inside of it.
     *
     * @param commentsToAdd
     *     The comments to add.
     */
    void comments(Iterable<String> commentsToAdd);

    /**
     * Gets all comments <em>associated</em> with this {@link Syntax} unit.
     * <p/>
     * A unit is associated with all comments that directly precede it. In the case of selectors, both the {@link Selector} object
     * and the first {@link SimpleSelector} within the {@link Selector} object will return the same comments.
     *
     * @return The list of comments. Never returns null.
     */
    List<Comment> comments();

    /**
     * Sets the current broadcast status. For internal use only, <strong>do not call directly</strong>.
     *
     * @param status
     *     The new status.
     *
     * @return this, for chaining.
     */
    Syntax status(Status status);

    /**
     * Gets the current broadcast status of this unit.
     * <p/>
     * This primarily determines whether this unit should be broadcasted again, given that each unit should be broadcasted at most
     * once per phase.
     *
     * @return The current broadcast status.
     */
    Status status();

    /**
     * Specifies the {@link Broadcaster} to use for broadcasting inner or child {@link Syntax} units.
     *
     * @param broadcaster
     *     Used to broadcast new {@link Syntax} units.
     *
     * @return this, for chaining.
     */
    Syntax broadcaster(Broadcaster broadcaster);

    /**
     * Gets the {@link Broadcaster} to use for broadcasting inner or child {@link Syntax} units.
     *
     * @return The {@link Broadcaster} to use for broadcasting inner or child {@link Syntax} units.
     */
    Broadcaster broadcaster();

    /**
     * Broadcasts all child units using the given {@link Broadcaster}.
     * <p/>
     * This is primarily used for dynamically created {@link Syntax} units that have child or inner units. When the parent unit
     * itself is broadcasted, this method should be called on the parent unit in order to propagate the broadcast event to the
     * children, ensuring that each child unit is properly broadcasted as well.
     * <p/>
     * This differs from the usage of {@link #broadcaster(Broadcaster)}. Parent units already in the tree will utilize the {@link
     * Broadcaster} from {@link #broadcaster(Broadcaster)} to broadcast child units as they are added. Broadcast propagation is
     * <em>not</em> needed for those child units. In contrast, parent units <b>not currently</b> in the tree are the ones that
     * need this method. It should be called when the parent unit is eventually broadcasted to ensure that any previously added
     * children are broadcasted as well.
     *
     * @param broadcaster
     *     Use this {@link Broadcaster} to broadcast all unbroadcasted child units.
     *
     * @see Broadcaster#broadcast(Syntax, boolean)
     */
    void propagateBroadcast(Broadcaster broadcaster);
}
