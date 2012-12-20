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

public class Filters {

    public static Filter handler(final Handler handler) {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                handler.handle(req, resp);
            }
        };
    }

    public static Filter returnStatus(final Class<? extends Exception> clazz, final Handler handler) {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                try{
                    chain.handle(req, resp);
                } catch (Exception e) {
                    if (clazz.isInstance(e))
                        handler.handle(req, resp);
                    else
                        throw e;
                }
            }
        };
    }


}
