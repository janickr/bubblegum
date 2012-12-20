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

import java.util.Collections;

public class Chain {
    private Route route;
    private Filter filter;
    private Chain next;

    public Chain() {
    }

    public Chain(Route route, Filter filter) {
        this(route, filter, null);
    }

    public Chain(Route route, Filter filter, Chain next) {
        this.route = route;
        this.filter = filter;
        this.next = next;
    }

    public void handle(Request request, Response response) throws Exception {
        Match match = getMatch(request);
        if (filter != null && match.isMatch()) {
            filter.handle(new Request(request.raw(), match.getParams()), response, next);
        }
        else if (next != null) next.handle(request, response);
    }

    private Match getMatch(Request request) {
        if (route == null) return Match.match(Collections.<String, String>emptyMap());
        return route.getMatch(request);
    }


    public Chain append(Chain chain) {
        return (next != null)
                ? new Chain(this.route, this.filter, next.append(chain))
                : new Chain(this.route, this.filter, chain);
    }
}
