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

package com.salesforce.omakase.broadcast.emitter;

import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.selector.ClassSelector;
import com.salesforce.omakase.ast.selector.SimpleSelector;
import com.salesforce.omakase.broadcast.annotation.Observe;
import com.salesforce.omakase.broadcast.annotation.Rework;
import com.salesforce.omakase.error.ThrowingErrorManager;
import com.salesforce.omakase.plugin.Plugin;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link Emitter}
 *
 * @author nmcwilliams
 */
@SuppressWarnings({"UnusedParameters", "JavaDoc"})
public class EmitterTest {
    @Test
    public void defaultPhase() {
        Emitter emitter = new Emitter();
        assertThat(emitter.phase()).isSameAs(SubscriptionPhase.PROCESS);
    }

    @Test
    public void setAndGetPhase() {
        Emitter emitter = new Emitter();
        emitter.phase(SubscriptionPhase.VALIDATE);
        assertThat(emitter.phase()).isSameAs(SubscriptionPhase.VALIDATE);
    }

    @Test
    public void hierarchy() {
        Emitter emitter = new Emitter();
        EmitterPlugin plugin = new EmitterPlugin();
        emitter.register(plugin);
        emitter.phase(SubscriptionPhase.PROCESS);

        emitter.emit(new ClassSelector("test"), new ThrowingErrorManager());

        assertThat(plugin.calledClassSelector).isTrue();
        assertThat(plugin.calledSimpleSelector).isTrue();
    }

    @Test
    public void samePluginTwice() {
        Emitter emitter = new Emitter();
        EmitterPlugin2 plugin = new EmitterPlugin2();
        emitter.register(plugin);
        emitter.register(plugin);

        emitter.emit(new ClassSelector("test"), new ThrowingErrorManager());
        assertThat(plugin.count).isEqualTo(1);
    }

    @Test
    public void maintainsRegistrationOrder() {
        List<Plugin> list = Lists.newArrayList();
        TestOrder1 t1 = new TestOrder1(list);
        TestOrder2 t2 = new TestOrder2(list);
        TestOrder3 t3 = new TestOrder3(list);
        TestOrder4 t4 = new TestOrder4(list);
        TestOrder5 t5 = new TestOrder5(list);

        Emitter emitter = new Emitter();
        emitter.register(t1);
        emitter.register(t2);
        emitter.register(t3);
        emitter.register(t4);
        emitter.register(t5);

        emitter.emit(new ClassSelector("test"), new ThrowingErrorManager());
        assertThat(list).containsExactly(t1, t2, t3, t4, t5);
    }

    @Test
    public void maintainsRegistrationOrderInterface() {
        List<Plugin> list = Lists.newArrayList();
        TestIntfOrder1 t1 = new TestIntfOrder1(list);
        TestIntfOrder2 t2 = new TestIntfOrder2(list);

        Emitter emitter = new Emitter();
        emitter.register(t1);
        emitter.register(t2);

        emitter.emit(new ClassSelector("test"), new ThrowingErrorManager());
        assertThat(list).containsExactly(t1, t1, t2, t2);
    }

    public static final class EmitterPlugin implements Plugin {
        boolean calledSimpleSelector;
        boolean calledClassSelector;

        @Observe
        public void simpleSelector(SimpleSelector s) {
            this.calledSimpleSelector = true;
        }

        @Observe
        public void classSelector(ClassSelector s) {
            this.calledClassSelector = true;
        }
    }

    public static final class EmitterPlugin2 implements Plugin {
        int count;

        @Rework
        public void preprocess(ClassSelector cs) {
            count++;
        }
    }

    public static final class TestOrder1 implements Plugin {
        private final List<Plugin> list;

        public TestOrder1(List<Plugin> list) { this.list = list; }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }

    public static final class TestOrder2 implements Plugin {
        private final List<Plugin> list;

        public TestOrder2(List<Plugin> list) { this.list = list; }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }

    public static final class TestOrder3 implements Plugin {
        private final List<Plugin> list;

        public TestOrder3(List<Plugin> list) { this.list = list; }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }

    public static final class TestOrder4 implements Plugin {
        private final List<Plugin> list;

        public TestOrder4(List<Plugin> list) { this.list = list; }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }

    public static final class TestOrder5 implements Plugin {
        private final List<Plugin> list;

        public TestOrder5(List<Plugin> list) { this.list = list; }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }

    public static final class TestIntfOrder1 implements Plugin {
        private final List<Plugin> list;

        public TestIntfOrder1(List<Plugin> list) { this.list = list; }

        @Observe
        public void simpleSelector(SimpleSelector s) {
            list.add(this);
        }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }

    public static final class TestIntfOrder2 implements Plugin {
        private final List<Plugin> list;

        public TestIntfOrder2(List<Plugin> list) { this.list = list; }

        @Observe
        public void simpleSelector(SimpleSelector s) {
            list.add(this);
        }

        @Observe
        public void observe(ClassSelector cs) {
            list.add(this);
        }
    }
}
