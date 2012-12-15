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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Match {
    private boolean match;
    private Map<String, String> params = new HashMap<String, String>();

    public static Match create(Route route, HttpServletRequest request) {
        if (!route.getMethod().equalsIgnoreCase(request.getMethod())) return new Match();
        if (!request.getHeader("Accept").contains(route.getContentType())) return new Match();

        List<String> parameterNames = route.getParamNames();

        Matcher matcher = createMatcher(route, request);

        Match match = new Match();
        if (matcher.matches()) {
            match.match = true;
            for (int i = 0; i < matcher.groupCount(); i++) {
                match.params.put(parameterNames.get(i), matcher.group(i+1));
            }
        }

        return match;
    }

    private static Matcher createMatcher(Route route, HttpServletRequest request) {
        return route.getPattern().matcher(request.getRequestURI());
    }

    public boolean isMatch() {
        return match;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
