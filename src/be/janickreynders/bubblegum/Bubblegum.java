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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;

public class Bubblegum implements javax.servlet.Filter {
    private static Logger LOG = Logger.getLogger(Bubblegum.class.getName());
    private Config config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        App routes = getRoutes(filterConfig);

        LOG.log(Level.INFO, "App.createConfig");
        config = routes.createConfig();
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        try {
            handle(servletRequest, servletResponse, filterChain);
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handle(ServletRequest servletRequest, ServletResponse servletResponse, final FilterChain filterChain) throws Exception {
        Request request = new Request((HttpServletRequest) servletRequest);
        Response response = new Response((HttpServletResponse) servletResponse);

        Object flag = request.attribute(getClass().getName() + ".handle");
        try {
            request.attribute(getClass().getName()+".handle", TRUE);
            config.buildChain(request, toChain(filterChain), flag == null).handle(request, response);
        } finally {
            request.attribute(getClass().getName() + ".handle", flag);
        }

    }

    private Chain toChain(final FilterChain filterChain) {
        return new Chain(new Filter() {
                            @Override
                            public void handle(Request req, Response resp, Chain chain) throws Exception {
                                filterChain.doFilter(req.raw(), resp.raw());
                            }
                         }, null, null);
    }


    protected App getRoutes(FilterConfig filterConfig) throws ServletException {
        LOG.log(Level.INFO, "Reading filterconfig");
        try {
            String name = filterConfig.getInitParameter("app");
            return (App) Class.forName(name).newInstance();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error creating App", e);
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
    }

}
