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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static be.janickreynders.bubblegum.Filters.handler;
import static be.janickreynders.bubblegum.Matchers.*;

public class Config {
    private LinkedList<Guard> filters = new LinkedList<Guard>();
    private List<Guard> handlers = new ArrayList<Guard>();


    public Chain buildChain(Request req, Chain originalFilterChain) {
        return wrapWithFilters(req, findMatchingHandler(req, originalFilterChain));
    }

    private Chain wrapWithFilters(Request req, Chain chain) {
        for (Iterator<Guard> iterator = filters.descendingIterator(); iterator.hasNext(); ) {
            chain = iterator.next().wrapChain(req, chain);
        }
        return chain;
    }

    public Chain findMatchingHandler(Request req, Chain originalFilterChain) {
        for (Guard guard : handlers) {
            Chain chain = guard.wrapChain(req, null);
            if (chain != null) {
                return chain;
            }
        }
        return originalFilterChain;
    }

    public void get(String route, Handler handler) {
        get(route, all(), handler);
    }

    public void put(String route, Handler handler) {
        put(route, all(), handler);
    }

    public void delete(String route, Handler handler) {
        delete(route, all(), handler);
    }

    public void post(String route, Handler handler) {
        post(route, all(), handler);
    }

    public void get(String route, RequestMatcher matcher, Handler handler) {
        route("get", route, matcher, handler);
    }

    public void put(String route, RequestMatcher matcher, Handler handler) {
        route("put", route, matcher, handler);
    }

    public void delete(String route, RequestMatcher matcher, Handler handler) {
        route("delete", route, matcher, handler);
    }

    public void post(String route, RequestMatcher matcher, Handler handler) {
        route("post", route, matcher, handler);
    }

    public void route(String httpMethod, String route, RequestMatcher matcher, Handler handler) {
        route(route, method(httpMethod).and(matcher), handler);
    }

    public void route(String route, RequestMatcher matcher, Handler handler) {
        route(matchRoute(route).and(matcher), handler);
    }

    public void route(RequestMatcher matcher, Handler handler) {
        handlers.add(new Guard(matcher, handler(handler)));
    }

    public void apply(String route, RequestMatcher matcher, Filter filter) {
        apply(matchRoute(route).and(matcher), filter);
    }

    public void apply(String route, Filter filter) {
        apply(route, all(), filter);
    }

    public void apply(Filter filter) {
        apply(all(), filter);
    }

    public void apply(RequestMatcher matcher, Filter filter) {
        filters.add(new Guard(matcher, filter));
    }
}
