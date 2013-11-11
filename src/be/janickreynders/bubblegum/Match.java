/*
 * MIT license
 *
 * Copyright (c) 2012 Janick Reynders
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package be.janickreynders.bubblegum;

import java.util.HashMap;
import java.util.Map;

public class Match {
    private static Match NO_MATCH = new Match(false);
    private static Match MATCH = new Match(true);

    private boolean match;
    private Map<String, String> params = new HashMap<String, String>();

    private Match(boolean b) {
        match = b;
    }

    public static Match match(Map<String, String> params) {
        Match match = new Match(true);
        match.params = params;
        return match;
    }

    public static Match noMatch() {
        return NO_MATCH;
    }

    public static Match match() {
        return MATCH;
    }

    public static Match when(boolean b) {
        return b ? MATCH : NO_MATCH;
    }

    public boolean isMatch() {
        return match;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Match and(Match other) {
        if (match && other.match) return match(bothParams(this, other));
        return NO_MATCH;
    }

    public Match or(Match other) {
        if (match || other.match) return match(bothParams(this, other));
        return NO_MATCH;
    }

    public Match negate() {
        return new Match(!match);
    }

    private Map<String, String> bothParams(Match match, Match other) {
        Map<String, String> params = new HashMap<String, String>();
        params.putAll(match.params);
        params.putAll(other.params);
        return params;
    }
}
