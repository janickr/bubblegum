Bubblegum - a micro web framework for java
==========================================

Very small web framework: only 25KB, does not depend on jars other than the servlet api.
It's only purpose is to match routes to handlers.

Usage
-----

1) Implement the App interface

```java
package be.janickreynders.test;

import be.janickreynders.bubblegum.*;

public class TestApp implements App {
    @Override
    public Config createConfig() {
        Config on = new Config();

        on.get("/hello/:name", new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.ok("<html><body>Hello " + req.param("name")+ "</html></body>");
                // or req.forward("hello.jsp", resp);
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
            <param-value>be.janickreynders.test.TestApp</param-value>
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

[MIT License]: https://github.com/janickr/bubblegum/raw/master/LICENSE.txt

