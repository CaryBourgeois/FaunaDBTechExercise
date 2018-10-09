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
 *  This part of the exercise creates the schema or classes and indexes for
 *  customers. The it loads the instances from teh JSON file into the DB.
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

    private static void createSchema(FaunaClient client) throws Exception {
        /*
         * Create an class to hold customers
         */
        Value result = client.query(
                CreateClass(Obj("name", Value("customers")))
        ).get();
        logger.info("Created customers class :: {}", toPrettyJson(result));

        /*
         * Create two indexes here. The first index is to query customers when you know specific id's.
         * The second is used to query customers by range. Examples of each type of query are presented
         * below.
         */
        result = client.query(
                Arr(
                        // This index supports primary key access. Equality constraints only
                        CreateIndex(
                                Obj(
                                        "name", Value("customer_by_id"),
                                        "source", Class(Value("customers")),
                                        "unique", Value(true),
                                        "terms", Arr(Obj("field", Arr(Value("data"), Value("id"))))
                                )
                        ),
                        // This index will let us search for specific values. See the range query Example.
                        CreateIndex(
                                Obj(
                                        "name", Value("customer_id_filter"),
                                        "source", Class(Value("customers")),
                                        "unique", Value(true),
                                        "values", Arr(
                                                Obj("field", Arr(Value("data"), Value("id"))),
                                                Obj("field", Arr(Value("ref")))
                                        )
                                )
                        )
                )
        ).get();
        logger.info("Created \'customer_by_id\' index & \'customer_id_filter\' index :: {}", toPrettyJson(result));
    }

    private static void loadCustomers(FaunaClient client, String jsonFilePath) throws Exception {
        //
        // This is cheesy. If the file was really big you would stream it
        // I am cheating by reading the whole thing and then parsing
        //
        JsonNode rootNode = mapper.readTree(new File(jsonFilePath));
        Iterator<JsonNode> it = rootNode.elements();

        ObjectMapper objectMapper = getMapper();
        while(it.hasNext()) {

            Customer customer = objectMapper.readValue(it.next().toString(), Customer.class);

            client.query(
                    Create(
                            Class(Value("customers")),
                            Obj("data", Value(customer))
                    )
            ).get();    // this makes are call Async. We could allways catch the futures and check later.
        }
        logger.info("Loaded {} customer records.", rootNode.size());
    }

    public static void main(String[] args)  throws Exception {
        /*
         * Create the DB specific DB client using the DB specific key just created.
         */
        FaunaClient client = FaunaClient.builder()
                .withEndpoint("https://db.fauna.com")
                .withSecret("fnAC876gXfACDX1EPRCxzWyoc6dBcgXFUOitDsSa") // your DB specific access string goes here
                .build();
        logger.info("Connected to FaunaDB");

        createSchema(client);

        loadCustomers(client, "./northwinds-json/customers.json");

        //
        // Just to keep things neat and tidy, close the client connections
        //
        client.close();
        logger.info("Disconnected from FaunaDB");

        // add this at the end of execution to make things shut down nicely
        System.exit(0);

    }
}
