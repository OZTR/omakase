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

package com.salesforce.omakase.test.util.perf;

import com.salesforce.omakase.Omakase;

/**
 * Omakase, full mode.
 *
 * @author nmcwilliams
 */
public final class OmakaseFull implements PerfTestParser {
    @Override
    public String code() {
        return "full";
    }

    @Override
    public String name() {
        return "omakase[full]";
    }

    @Override
    public void parse(String input) {
        Omakase.source(input).request(PluginSet.normal()).process();
    }
}
