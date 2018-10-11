/*
 * Copyright 2018 Fauna, Inc.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License"); you may
 * not use this software except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package fauna.techexercise;

/******************************************************************************
 *  This part of the exercise creates the classes and class level indexes for
 *  the Northwind JSON data. This is an example of how you could do this in
 *  code. Alternatively you could accomplish the same through the Fauna
 *  command line tool. For Example you could use the following commands:
 *
 *  $ fauna create-database Northwind
 *  $ fauna create-key Northwind
 *  $ fauna shell Northwind
 *  Northwind> CreateClass({ name: “categories” })
 *  Northwind> CreateIndex({name: "categories_all", source: Class("categories"), unique: true})
 *
 *  Northwind> CreateClass({ name: “products” })
 *  Northwind> CreateIndex({name: "products_all", source: Class("products"), unique: true})
 *
 *  Northwind> CreateClass({ name: “customers” })
 *  Northwind> CreateIndex({name: "customers_all", source: Class("customers"), unique: true})
 *
 *  With those command successfully executed you can then load the data in a
 *  manner of your choosing including the faunadb-importer tool located here:
 *  https://github.com/fauna/faunadb-importer
 *****************************************************************************/

/*
 * These imports are for basic functionality around logging and JSON handling and Futures.
 * They should best be thought of as a convenience items for our exercises.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.File;
import java.util.Iterator;

/*
 * These are the required imports for Fauna.
 *
 * For these examples we are using the 2.1.0 version of the JVM driver. Also notice that we aliasing
 * the query and values part of the API to make it more obvious we we are using Fauna functionality.
 *
 */
import com.faunadb.client.*;
import com.faunadb.client.types.*;
import static com.faunadb.client.query.Language.*;

public class DataLoading {
    /*
     * Connection variables:
     * Endpoint will depend ow whether you are using cloud or local
     *  - https://db.fauuna.com -- for Fauna DB Cloud
     *  - http://localhost:8443 -- if you are using a local docker instance
     *
     * Secret is the DD specific secret that you generated either from the
     * GettingStarted.java execution or the running the above fauna command
     * line commands.
     */
//    private static String endpoint = "https://db.fauna.com";
    private static String endpoint = "http://localhost:8443";
    private static String secret = "fnAC9FsHMiACAHMG0pNOe93XQ9TVVdHZMZwPbFJ0";

    private static final Logger logger = LoggerFactory.getLogger(GettingStarted.class);

    private static ObjectMapper mapper = getMapper();

    private static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    private static String toPrettyJson(Value value) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    private static void createClass(FaunaClient client, String className) throws Exception {
        /*
         * Create the class to store the data instances
         */
        Value result = client.query(
                CreateClass(Obj("name", Value(className)))
        ).get();
        logger.info("Created data class : {} :: \n{}", className, toPrettyJson(result));
    }

    private static void createClassIndex(FaunaClient client, String className) throws Exception {
        /*
         * Create a class level index. This is really a convenience feature that lets us
         * evaluate if the data loaded properly.
         */
        String classIndexName = className + "_all";
        Value result = client.query(
                // This index supports primary key access. Equality constraints only
                    CreateIndex(
                            Obj(
                                    "name", Value(classIndexName),
                                    "source", Class(Value(className))
                            )
                    )
        ).get();
        logger.info("Created {} index :: \n{}", classIndexName, toPrettyJson(result));
    }

    private static void loadData(FaunaClient client,
                                 Class dataClass,
                                 String dataTypeName,
                                 String jsonFilePath) throws Exception {
        //
        // This is cheesy. If the file was really big you would stream it
        // I am cheating by reading the whole thing and then parsing
        //
        JsonNode rootNode = mapper.readTree(new File(jsonFilePath));
        Iterator<JsonNode> it = rootNode.elements();

        ObjectMapper objectMapper = getMapper();
        while(it.hasNext()) {

            Object data =  dataClass.newInstance();
            data = objectMapper.readValue(it.next().toString(), dataClass);

            client.query(
                    Create(
                            Class(Value(dataTypeName)),
                            Obj("data", Value(data))
                    )
            ).get();    // this makes the call Async. We could always catch the futures and check later.
        }
        logger.info("Loaded {} {} records.", rootNode.size(), dataTypeName);

    }

    public static void main(String[] args)  throws Exception {
        /*
         * Create the DB specific DB client using the DB specific key just created.
         */
        FaunaClient client = FaunaClient.builder()
                .withEndpoint(endpoint)
                .withSecret(secret)
                .build();
        logger.info("Connected to FaunaDB");

        createClass(client, "categories");
        createClassIndex(client, "categories");
        loadData(client, Category.class, "categories","./northwinds-json/categories.json");

        createClass(client, "products");
        createClassIndex(client, "products");
        loadData(client, Product.class, "products", "./northwinds-json/products.json");

        createClass(client, "customers");
        createClassIndex(client, "customers");
        loadData(client, Customer.class, "customers", "./northwinds-json/customers.json");

        //
        // Just to keep things neat and tidy, close the client connections
        //
        client.close();
        logger.info("Disconnected from FaunaDB");

        // add this at the end of execution to make things shut down nicely
        System.exit(0);

    }
}
