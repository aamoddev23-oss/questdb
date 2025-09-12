/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.test.cutlass.http;

import io.questdb.test.cutlass.http.HttpQueryTestBuilder.HttpClientCode;
import io.questdb.test.tools.TestUtils;
import org.junit.Test;

public class HttpBindVariableTest {

    @Test
    public void testNamedBindVariablesWithTypes() throws Exception {
        new HttpQueryTestBuilder()
                .withTempFolder(TestUtils.temp())
                .withWorkerCount(1)
                .run((engine, sqlExecutionContext) -> {
                    // Create test table
                    engine.ddl(
                            "CREATE TABLE test_table (" +
                            "id int, " +
                            "name string, " +
                            "price double, " +
                            "active boolean" +
                            ")",
                            sqlExecutionContext
                    );

                    // Insert test data
                    engine.insert(
                            "INSERT INTO test_table VALUES " +
                            "(1, 'Product A', 10.5, true), " +
                            "(2, 'Product B', 20.0, false), " +
                            "(3, 'Product C', 15.75, true)",
                            sqlExecutionContext
                    );
                }, (code, response, httpConnectionContext) -> {
                    // Test named bind variables with type inference
                    code.sendAndReceive(
                            "GET /query?query=select%20*%20from%20test_table%20where%20id%20=%20$id&$id=1 HTTP/1.1\r\n" +
                            "Host: localhost:9000\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n",
                            "HTTP/1.1 200 OK\r\n" +
                            "Server: questDB/1.0\r\n" +
                            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
                            "Transfer-Encoding: chunked\r\n" +
                            "Content-Type: application/json; charset=utf-8\r\n" +
                            "Keep-Alive: timeout=5, max=10000\r\n" +
                            "\r\n" +
                            "5d\r\n" +
                            "{\"query\":\"select * from test_table where id = $id\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"},{\"name\":\"name\",\"type\":\"STRING\"},{\"name\":\"price\",\"type\":\"DOUBLE\"},{\"name\":\"active\",\"type\":\"BOOLEAN\"}],\"timestamp\":-1,\"dataset\":[[1,\"Product A\",10.5,true]],\"count\":1}\r\n" +
                            "00\r\n" +
                            "\r\n"
                    );
                });
    }

    @Test
    public void testPositionalBindVariables() throws Exception {
        new HttpQueryTestBuilder()
                .withTempFolder(TestUtils.temp())
                .withWorkerCount(1)
                .run((engine, sqlExecutionContext) -> {
                    // Create test table
                    engine.ddl(
                            "CREATE TABLE test_table (" +
                            "id int, " +
                            "name string, " +
                            "price double" +
                            ")",
                            sqlExecutionContext
                    );

                    // Insert test data
                    engine.insert(
                            "INSERT INTO test_table VALUES " +
                            "(1, 'Product A', 10.5), " +
                            "(2, 'Product B', 20.0), " +
                            "(3, 'Product C', 15.75)",
                            sqlExecutionContext
                    );
                }, (code, response, httpConnectionContext) -> {
                    // Test positional bind variables
                    code.sendAndReceive(
                            "GET /query?query=select%20*%20from%20test_table%20where%20id%20=%20$1%20and%20price%20>%20$2&1=2&2=15.0 HTTP/1.1\r\n" +
                            "Host: localhost:9000\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n",
                            "HTTP/1.1 200 OK\r\n" +
                            "Server: questDB/1.0\r\n" +
                            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
                            "Transfer-Encoding: chunked\r\n" +
                            "Content-Type: application/json; charset=utf-8\r\n" +
                            "Keep-Alive: timeout=5, max=10000\r\n" +
                            "\r\n" +
                            "5b\r\n" +
                            "{\"query\":\"select * from test_table where id = $1 and price > $2\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"},{\"name\":\"name\",\"type\":\"STRING\"},{\"name\":\"price\",\"type\":\"DOUBLE\"}],\"timestamp\":-1,\"dataset\":[[2,\"Product B\",20.0]],\"count\":1}\r\n" +
                            "00\r\n" +
                            "\r\n"
                    );
                });
    }

    @Test
    public void testBooleanBindVariable() throws Exception {
        new HttpQueryTestBuilder()
                .withTempFolder(TestUtils.temp())
                .withWorkerCount(1)
                .run((engine, sqlExecutionContext) -> {
                    // Create test table
                    engine.ddl(
                            "CREATE TABLE test_table (" +
                            "id int, " +
                            "name string, " +
                            "active boolean" +
                            ")",
                            sqlExecutionContext
                    );

                    // Insert test data
                    engine.insert(
                            "INSERT INTO test_table VALUES " +
                            "(1, 'Product A', true), " +
                            "(2, 'Product B', false), " +
                            "(3, 'Product C', true)",
                            sqlExecutionContext
                    );
                }, (code, response, httpConnectionContext) -> {
                    // Test boolean bind variable
                    code.sendAndReceive(
                            "GET /query?query=select%20*%20from%20test_table%20where%20active%20=%20$active&$active=true HTTP/1.1\r\n" +
                            "Host: localhost:9000\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n",
                            "HTTP/1.1 200 OK\r\n" +
                            "Server: questDB/1.0\r\n" +
                            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
                            "Transfer-Encoding: chunked\r\n" +
                            "Content-Type: application/json; charset=utf-8\r\n" +
                            "Keep-Alive: timeout=5, max=10000\r\n" +
                            "\r\n" +
                            "60\r\n" +
                            "{\"query\":\"select * from test_table where active = $active\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"},{\"name\":\"name\",\"type\":\"STRING\"},{\"name\":\"active\",\"type\":\"BOOLEAN\"}],\"timestamp\":-1,\"dataset\":[[1,\"Product A\",true],[3,\"Product C\",true]],\"count\":2}\r\n" +
                            "00\r\n" +
                            "\r\n"
                    );
                });
    }

    @Test
    public void testStringBindVariable() throws Exception {
        new HttpQueryTestBuilder()
                .withTempFolder(TestUtils.temp())
                .withWorkerCount(1)
                .run((engine, sqlExecutionContext) -> {
                    // Create test table
                    engine.ddl(
                            "CREATE TABLE test_table (" +
                            "id int, " +
                            "name string" +
                            ")",
                            sqlExecutionContext
                    );

                    // Insert test data
                    engine.insert(
                            "INSERT INTO test_table VALUES " +
                            "(1, 'Product A'), " +
                            "(2, 'Product B'), " +
                            "(3, 'Product C')",
                            sqlExecutionContext
                    );
                }, (code, response, httpConnectionContext) -> {
                    // Test string bind variable
                    code.sendAndReceive(
                            "GET /query?query=select%20*%20from%20test_table%20where%20name%20=%20$name&$name=Product%20B HTTP/1.1\r\n" +
                            "Host: localhost:9000\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n",
                            "HTTP/1.1 200 OK\r\n" +
                            "Server: questDB/1.0\r\n" +
                            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
                            "Transfer-Encoding: chunked\r\n" +
                            "Content-Type: application/json; charset=utf-8\r\n" +
                            "Keep-Alive: timeout=5, max=10000\r\n" +
                            "\r\n" +
                            "55\r\n" +
                            "{\"query\":\"select * from test_table where name = $name\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"},{\"name\":\"name\",\"type\":\"STRING\"}],\"timestamp\":-1,\"dataset\":[[2,\"Product B\"]],\"count\":1}\r\n" +
                            "00\r\n" +
                            "\r\n"
                    );
                });
    }

    @Test
    public void testNullBindVariable() throws Exception {
        new HttpQueryTestBuilder()
                .withTempFolder(TestUtils.temp())
                .withWorkerCount(1)
                .run((engine, sqlExecutionContext) -> {
                    // Create test table
                    engine.ddl(
                            "CREATE TABLE test_table (" +
                            "id int, " +
                            "name string" +
                            ")",
                            sqlExecutionContext
                    );

                    // Insert test data with null value
                    engine.insert(
                            "INSERT INTO test_table VALUES " +
                            "(1, 'Product A'), " +
                            "(2, null), " +
                            "(3, 'Product C')",
                            sqlExecutionContext
                    );
                }, (code, response, httpConnectionContext) -> {
                    // Test null bind variable
                    code.sendAndReceive(
                            "GET /query?query=select%20*%20from%20test_table%20where%20name%20=%20$name&$name=null HTTP/1.1\r\n" +
                            "Host: localhost:9000\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n",
                            "HTTP/1.1 200 OK\r\n" +
                            "Server: questDB/1.0\r\n" +
                            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
                            "Transfer-Encoding: chunked\r\n" +
                            "Content-Type: application/json; charset=utf-8\r\n" +
                            "Keep-Alive: timeout=5, max=10000\r\n" +
                            "\r\n" +
                            "53\r\n" +
                            "{\"query\":\"select * from test_table where name = $name\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"},{\"name\":\"name\",\"type\":\"STRING\"}],\"timestamp\":-1,\"dataset\":[[2,null]],\"count\":1}\r\n" +
                            "00\r\n" +
                            "\r\n"
                    );
                });
    }
}