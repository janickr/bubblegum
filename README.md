Bubblegum - a micro web framework for java
==========================================

Less than 50KB and does not depend on jars other than the servlet api.
It's only purpose is to match routes to handlers.

Bubblegum is inspired by [Spark], but there are important differences. In Bubblegum:
- Paths are matched case-sensitive, path parameters extracted without changing case
- Requests can be matched on any of its properties for example on the Accept or Content-type headers
- Handlers can throw exceptions
- Filters are more servlet Filter-like (but with more expressive filter mapping)
- JdbcHelper makes queries easy, ConnectionProvider provides request-scoped jdbc transactions (Optional - it's a bubblegum Filter)

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
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>FORWARD</dispatcher>
        <dispatcher>INCLUDE</dispatcher>
        <dispatcher>ERROR</dispatcher>
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
        on.get("/forward/me", forward("/forwarded.jsp"));

        // forward a request to a jsp in WEB-INF/jsp
        on.get("/forward/me2", forward("/WEB-INF/jsp/forwarded.jsp"));

        // forward a request to another handler
        on.get("/forward/again", forward("/textcontent"));

        // match the paths '/different/this' and '/different/some-other-thing'
        //   but not '/different/that/or/this'
        on.get("/different/*", forward("/forwarded.html"));

        // match any of '/multiple/this/levels', '/multiple/this/or/that/levels' ,...
        on.get("/multiple/**/levels", forward("/forwarded.txt"));

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
                String value = req.param("inputFieldName");

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
                // get params with req.param();
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
        on.apply(catchAndHandle(IllegalStateException.class, forward("/oops.jsp")));

        return on;
    }
}
```

Jdbc helper
-----------
JdbcHelper contains some convenience methods for querying a rdbms. If you configure ConnectionProvider as a bubblegum filter
it will always return the same connection during a request (the connection is bound to the thread).
After the request the transaction is committed (or rolled back in case the handler threw an exception)
and the connection will be closed.

```java
import be.janickreynders.bubblegum.*;
import be.janickreynders.bubblegum.jdbc.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static be.janickreynders.bubblegum.Matchers.accept;
import static be.janickreynders.bubblegum.jdbc.ConnectionProvider.withDbConnection;

public class JdbcExample implements App {
    @Override
    public Config createConfig() {
        final JdbcHelper db = new JdbcHelper(getDataSource());

        Config on = new Config();

        on.apply(new ConnectionProvider()); // ConnectionProvider is a filter that returns the same open connection during your request
                      // It will commit your transaction after the request and closes the connection

        on.post("/insertSomething", accept("text/html"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                String value1 = req.param("inputFieldName1");
                String value2 = req.param("inputFieldName2");
                db.update("insert into my_table (value1, value2) values (?, ?)", value1, value2);

                resp.ok("you inserted: " + value1 + " and " + value2);
            }
        });

        on.get("/queryValue2/:value1", accept("text/html"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                String value1 = req.param("value1");
                String value2 = db.getString("select value2 from my_table where value1 = ?", value1);

                resp.ok(value2);
            }
        });

        on.post("/doSomethingAsynchronousInOneTransaction", accept("text/html"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                executor.execute(withDbConnection(new Runnable() {
                    @Override
                    public void run() {
                        db.update("insert into this_table (some_col) values (?)", "one thing");
                        db.update("insert into that_table (some_col) values (?)", "another thing");
                    }
                }));

                resp.ok("it's running");
            }
        });

        return on;
    }

    private DataSource getDataSource() {
        try {
            return(DataSource) new InitialContext().lookup("java:/comp/env/jdbc/yourDatasourceNameInJNDI");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        // you could also just create your datasource here instead of having one configured in JNDI
    }

    public JdbcExample() {
        executor = Executors.newSingleThreadExecutor();
    }

    private final ExecutorService executor;
}
```

[MIT License]: https://github.com/janickr/bubblegum/raw/master/LICENSE.txt
[Spark]: https://github.com/perwendel/spark
