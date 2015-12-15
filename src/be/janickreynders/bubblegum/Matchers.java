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

public class Matchers {

    public static RequestMatcher method(final String httpMethod) {
        return new RequestMatcher() {
            @Override
            public Match matches(Request req) {
                return Match.when(httpMethod.equalsIgnoreCase(req.method()));
            }

            @Override
            public String toString() {
                return "method: " + httpMethod;
            }
        };
    }

    public static RequestMatcher header(final String headerName, final CharSequence value) {
        return new RequestMatcher() {
            @Override
            public Match matches(Request req) {
                return Match.when(req.header(headerName).contains(value));
            }

            @Override
            public String toString() {
                return headerName + ": " + value;
            }
        };
    }

    public static RequestMatcher accept(final CharSequence mimeType) {
        return new RequestMatcher() {
            @Override
            public Match matches(Request req) {
                String accept = req.header("Accept");
                return Match.when(accept.contains(mimeType) || accept.equals("*/*"));
            }

            @Override
            public String toString() {
                return "Accept: " + mimeType;
            }
        };
    }

    public static RequestMatcher contentType(CharSequence contentType) {
        return header("Content-Type", contentType);
    }

    public static RequestMatcher path(String path) {
        return new PathMatcher(path);
    }

    private static List<RequestMatcher> withoutNulls(RequestMatcher... matchers) {
        ArrayList<RequestMatcher> noNulls = new ArrayList<>(matchers.length);
        for (RequestMatcher matcher : matchers) {
            if (matcher != null) {
                noNulls.add(matcher);
            }
        }
        return noNulls;
    }

    public static RequestMatcher all(final RequestMatcher... requestMatchers) {
        final List<RequestMatcher> matchers = withoutNulls(requestMatchers);

        if (matchers.size() == 1) return matchers.get(0);
        return new RequestMatcher() {
            @Override
            public Match matches(Request req) {
                Map<String, String> params = new HashMap<>();

                for (RequestMatcher matcher : matchers) {
                    Match match = matcher.matches(req);
                    if (match.isMatch()) {
                        params.putAll(match.getParams());
                    } else {
                        return Match.noMatch();
                    }
                }
                return Match.match(params);
            }

            @Override
            public String toString() {
                return "all" + matchers;
            }
        };
    }

    public static RequestMatcher any(final RequestMatcher... requestMatchers) {
        final List<RequestMatcher> matchers = withoutNulls(requestMatchers);

        if (matchers.size() == 1) return matchers.get(0);
        return new RequestMatcher() {
            @Override
            public Match matches(Request req) {
                Map<String, String> params = new HashMap<>();
                boolean isMatch = false;

                for (RequestMatcher matcher : matchers) {
                    Match match = matcher.matches(req);
                    isMatch |= match.isMatch();
                    params.putAll(match.getParams());
                }
                return isMatch ? Match.match(params) : Match.noMatch();
            }

            @Override
            public String toString() {
                return "any" + matchers;
            }
        };
    }

    public static RequestMatcher not(final RequestMatcher matcher) {
        return new RequestMatcher() {
            @Override
            public Match matches(Request req) {
                return Match.when(!matcher.matches(req).isMatch());
            }

            @Override
            public String toString() {
                return "not(" + matcher + ")";
            }
        };
    }
}
