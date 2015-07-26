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
import java.util.logging.Level;
import java.util.logging.Logger;

import static be.janickreynders.bubblegum.Filters.handler;
import static be.janickreynders.bubblegum.Matchers.*;

public class Config {
    private static Logger LOG = Logger.getLogger(Config.class.getName());
    private LinkedList<Route> filters = new LinkedList<Route>();
    private List<Route> handlers = new ArrayList<Route>();


    public Chain buildChain(Request req, Chain originalFilterChain, boolean wrapWithFilters) {
        Chain matchingHandler = findMatchingHandler(req, originalFilterChain);
        return (wrapWithFilters) ? wrapWithFilters(req, matchingHandler) : matchingHandler;
    }

    private Chain wrapWithFilters(Request req, Chain chain) {
        for (Iterator<Route> iterator = filters.descendingIterator(); iterator.hasNext(); ) {
            chain = iterator.next().wrapChain(req, chain);
        }
        return chain;
    }

    public Chain findMatchingHandler(Request req, Chain originalFilterChain) {
        for (Route route : handlers) {
            Chain chain = route.wrapChain(req, null);
            if (chain != null) {
                return chain;
            }
        }
        return originalFilterChain;
    }

    public void get(String path, Handler handler) {
        get(path, null, handler);
    }

    public void put(String path, Handler handler) {
        put(path, null, handler);
    }

    public void delete(String path, Handler handler) {
        delete(path, null, handler);
    }

    public void post(String path, Handler handler) {
        post(path, null, handler);
    }

    public void get(String path, RequestMatcher matcher, Handler handler) {
        route("get", path, matcher, handler);
    }

    public void put(String path, RequestMatcher matcher, Handler handler) {
        route("put", path, matcher, handler);
    }

    public void delete(String path, RequestMatcher matcher, Handler handler) {
        route("delete", path, matcher, handler);
    }

    public void post(String path, RequestMatcher matcher, Handler handler) {
        route("post", path, matcher, handler);
    }

    public void route(String httpMethod, String path, RequestMatcher matcher, Handler handler) {
        route(path, method(httpMethod).and(matcher), handler);
    }

    public void route(String path, RequestMatcher matcher, Handler handler) {
        route(path(path).and(matcher), handler);
    }

    public void route(RequestMatcher matcher, Handler handler) {
        handlers.add(new Route(matcher, handler(handler)));
        log(matcher, handler);
    }

    public void apply(String route, RequestMatcher matcher, Filter filter) {
        apply(path(route).and(matcher), filter);
    }

    public void apply(String path, Filter filter) {
        apply(path, null, filter);
    }

    public void apply(Filter filter) {
        apply(all(), filter);
    }

    public void apply(RequestMatcher matcher, Filter filter) {
        filters.add(new Route(matcher, filter));
        log(matcher, filter);
    }

    private void log(RequestMatcher matcher, Object filterOrHandler) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "{0} -> {1}", new Object[] {matcher, callToString(filterOrHandler)});
        }
    }

    private String callToString(Object filterOrHandler) {
        try {
            filterOrHandler.getClass().getDeclaredMethod("toString");
            return filterOrHandler.toString();
        } catch (NoSuchMethodException e) {
            return filterOrHandler.getClass().getName();
        }
    }

}
