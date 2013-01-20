Bubblegum - a micro web framework for java
==========================================

Very small web framework: only 25KB, does not depend on jars other than the servlet api.
It's only purpose is to match routes to handlers.

Bubblegum is inspired by [Spark], but there are important differences. Bubblegum:
- matches paths case-sensitive
- can match requests on any of its properties for example on the Accept or Content-type headers
- Handlers can throw exceptions
- Filters are more servlet Filter-like (but with more expressive filter mapping)

Get the jar at <http://janickreynders.be/bubblegum>


Usage
-----

1) Implement the App interface

```java
package com.yourpackage.test;

import be.janickreynders.bubblegum.*;

public class TestApp implements App {
    @Override
    public Config createConfig() {
        Config on = new Config();

        on.get("/hello/:name", new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.ok("Hello " + req.param("name") + "!");
            }
        });

        return on;
    }
}
```

2) Add Bubblegum to your web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
           version="2.5">

    <filter>
        <filter-name>Bubblegum</filter-name>
        <filter-class>be.janickreynders.bubblegum.Bubblegum</filter-class>
        <init-param>
            <param-name>app</param-name>
            <param-value>com.yourpackage.test.TestApp</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>Bubblegum</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```

3) That's it! Deploy and point your browser to http://localhost/hello/chuck

Copyright and License
---------------------
Copyright &copy; 2012-, Janick Reynders. Licensed under [MIT License].


More Examples
-------------

```java
import be.janickreynders.bubblegum.*;

import javax.servlet.http.HttpServletResponse;

import static be.janickreynders.bubblegum.Filters.*;
import static be.janickreynders.bubblegum.Handlers.*;
import static be.janickreynders.bubblegum.Matchers.*;

public class Examples implements App {
    @Override
    public Config createConfig() {
        Config on = new Config();

        // forward a request to a jsp
        on.get("/forward/me", forward("forwarded.jsp"));

        // match the paths '/different/this' and '/different/some-other-thing'
        //   but not '/different/that/or/this'
        on.get("/different/*", forward("forwarded.html"));

        // match any of '/multiple/this/levels', '/multiple/this/or/that/levels' ,...
        on.get("/multiple/**/levels", forward("forwarded.txt"));

        // redirect to a different url
        on.get("/redirect/me", redirect("/redirected"));

        // using a path variable
        on.get("/collection/:id", new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.ok("you requested: " + req.param("id"));
            }
        });

        // set the content type of the response
        on.get("/textcontent", new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.type("text/plain");
                resp.ok("You are getting a text response");
            }
        });

        // post to a url
        on.post("/collection", new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                String value = req.queryParam("inputFieldName");

                resp.ok("you posted: " + value);
            }
        });

        // another http method
        on.delete("/collection/:id", new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.ok("you deleted: " + req.param("id"));
            }
        });


        // different response for different request Accept headers
        on.get("/variant", accept("text/html"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.vary("Accept");
                resp.ok("<html><body> This is the html variant </body></html>");
            }
        });

        on.get("/variant", accept("application/json"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.vary("Accept");
                resp.ok("{ \"message\": \"This is the json variant\" }");
            }
        });

        // interpreting different request body content types
        on.get("/requestbody", contentType("application/x-www-form-urlencoded"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                // get params with req.queryParam();
            }
        });

        on.get("/requestbody", contentType("application/json"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                // parse the json in req.body()
            }
        });

        // match multiple http methods, and return a status code
        on.route("/multiple-methods", any(method("post"), method("put"), method("delete")),
            status(HttpServletResponse.SC_FORBIDDEN));


        /********************** filter examples **********************/

        // cache /js/thirdparty/** forever
        on.apply("/js/thirdparty/**", cacheNeverExpires());

        // forward to an error jsp on a certain exception
        on.apply(catchAndHandle(IllegalStateException.class, forward("oops.jsp")));

        return on;
    }
}
```

[MIT License]: https://github.com/janickr/bubblegum/raw/master/LICENSE.txt
[Spark]: https://github.com/perwendel/spark
