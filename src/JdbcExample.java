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

import be.janickreynders.bubblegum.App;
import be.janickreynders.bubblegum.Config;
import be.janickreynders.bubblegum.jdbc.ConnectionProvider;
import be.janickreynders.bubblegum.jdbc.JdbcHelper;

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

        on.post("/insertSomething", accept("text/html"), (req, resp) -> {
            String value1 = req.param("inputFieldName1");
            String value2 = req.param("inputFieldName2");
            db.update("insert into my_table (value1, value2) values (?, ?)", value1, value2);

            resp.ok("you inserted: " + value1 + " and " + value2);
        });

        on.get("/queryValue2/:value1", accept("text/html"), (req, resp) -> {
            String value1 = req.param("value1");
            String value2 = db.getString("select value2 from my_table where value1 = ?", value1);

            resp.ok(value2);
        });

        on.post("/doSomethingAsynchronousInOneTransaction", accept("text/html"), (req, resp) -> {
            executor.execute(withDbConnection(() -> {
                db.update("insert into this_table (some_col) values (?)", "one thing");
                db.update("insert into that_table (some_col) values (?)", "another thing");
            }));

            resp.ok("it's running");
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