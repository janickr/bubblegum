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
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static be.janickreynders.bubblegum.Matchers.any;
import static be.janickreynders.bubblegum.Matchers.method;

public class Config {
    private List<Route> routes = new ArrayList<Route>();
    private List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();


    boolean handle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        for (Route route : routes) {
            Match match = route.getMatch(req);
            if (match.isMatch()) {
                callHandler(route, match, req, resp);

                return true;
            }
        }
        return false;
    }

    private void callHandler(Route route, Match match, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Request request = new Request(req, match.getParams());
        Response response = new Response(resp);
        try {
            route.getHandler().handle(request, response);
        } catch (Exception e) {
            for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                if (exceptionHandler.matches(e)) {
                    exceptionHandler.handle(request, response);
                    return;
                }
            }
            throw e;
        }
    }

    public void get(String route, Handler handler) {
        get(route, any(), handler);
    }

    public void put(String route, Handler handler) {
        put(route, any(), handler);
    }

    public void delete(String route, Handler handler) {
        delete(route, any(), handler);
    }

    public void post(String route, Handler handler) {
        post(route, any(), handler);
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
        routes.add(new Route(route, handler, matcher));
    }

    public void exception(ExceptionMatcher matcher, Handler handler) {
        exceptionHandlers.add(new ExceptionHandler(matcher, handler));
    }
}
