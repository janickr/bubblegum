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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathMatcher extends RequestMatcher {
    private final List<String> paramNames;
    private final Pattern pattern;
    private final String path;


    public PathMatcher(String route) {
        this.path = route;
        this.paramNames = getParamNames(route);
        this.pattern = createPattern(route);
    }

    private static List<String> getParamNames(String route) {
        List<String> parameterNames = new ArrayList<String>();
        for (String part : route.split("/")) {
            if (part.startsWith(":") && part.length() > 1) parameterNames.add(part.substring(1));
        }
        return parameterNames;
    }

    private static Pattern createPattern(String route) {
        return Pattern.compile(route.replaceAll("(?<!\\*)\\*(?!\\*)", "[^/]+").replaceAll("\\*\\*", ".*").replaceAll(":\\w+", "([^/]+)") + "/?");
    }

    public Match matches(Request req) {
        Matcher regex = pattern.matcher(req.getPath());
        if (!regex.matches()) return Match.noMatch();

        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < regex.groupCount(); i++) {
            params.put(paramNames.get(i), regex.group(i+1));
        }
        return Match.match(params);
    }

    @Override
    public String toString() {
        return "path: " + path;
    }
}
