Bubblegum - a micro web framework for java
==========================================

Very lightweight web framework. It's only purpose is to match routes to handlers. No other dependencies.

Usage
-----

1) Implement the Routes interface

'''java
package be.janickreynders.test;

import be.janickreynders.bubblegum.*;

public class TestApp implements Routes {
    @Override
    public void init(Application app) {

        app.get("/hello/:name", "text/html", new Handler() {
            @Override
            public void handle(Request req, Response resp) {
                resp.ok("<html><body>Hello " + req.param("name")+ "</html></body>");
                // or req.forward("hello.jsp", resp);
            }
        });
    }
}
'''

2) Add BubblegumFilter to your web.xml

'''xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
           version="2.5">

    <filter>
        <filter-name>BubblegumFilter</filter-name>
        <filter-class>be.janickreynders.bubblegum.BubblegumFilter</filter-class>
        <init-param>
            <param-name>routes</param-name>
            <param-value>be.janickreynders.test.TestApp</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>BubblegumFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
'''

3) That's it! Deploy and point your browser to http://localhost/hello/chuck

Copyright and License
---------------------
Copyright &copy; 2012-, Janick Reynders. Licensed under [MIT License].

[MIT License]: https://github.com/janickr/shortcuttranslator/raw/master/LICENSE.txt

