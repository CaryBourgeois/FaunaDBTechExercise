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
 *  This part of the exercise creates a couple of indexes and shows how to page
 *  through a result set of a values only index. NOTE: The technique will not
 *  work with an index that defines terms. Term based indexes require that a
 *  value for the term(s) is specified when accessing the index.
 *****************************************************************************/

/*
 * These imports are for basic functionality around logging and JSON handling and Futures.
 * They should best be thought of as a convenience items for our exercises.
 */
import com.faunadb.client.query.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;

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

public class IndexAndQuery {
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

    private static final Logger logger = LoggerFactory.getLogger(IndexAndQuery.class);

    private static ObjectMapper mapper = getMapper();

    private static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    private static String toPrettyJson(Value value) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    private static void createIndex(FaunaClient client, Expr indexDef, boolean waitForIndexToBeActive) throws Exception {
        /*
         * This is a generalized example of Fauna Query Language composable nature.
         * The function accepts a type of Epr which will be the definition of the index.
         */
        Value result = client.query(
                CreateIndex(indexDef)
        ).get();
        String indexName = result.at("name").to(String.class).get();
        logger.info("Created index: {} \n{}", indexName, toPrettyJson(result));

        if (waitForIndexToBeActive) {
            waitForIndexToBuild(client, indexName);
        }
    }

    /*
     * When you create a new index it can take it a little while to become active.
     * In other words, it may take it a whil before it has fully added all instances
     * of the class being indexed. If you try to query before it is active you may
     * not see all instances of the data.
     */
    private static void waitForIndexToBuild(FaunaClient client, String indexName) throws Exception {
        int totalWaitMillis = 120000;   // maximum time to wait for build 2 minutes
        int intervalMillis = 5000;      // check every 5 seconds
        int maxIntervals = totalWaitMillis/intervalMillis;

        logger.info("Waiting for index {} to become active...", indexName);
        for(int i = 0; i < maxIntervals; i++) {
            try {
                Value result = client.query(
                        Select(Value("active"), Get(Index(indexName)))
                ).get();
                if (result.to(Boolean.class).get()) {
                    logger.info("Index {} is active.", indexName);
                    return;
                }
                Thread.sleep(intervalMillis);
            } catch(InterruptedException ie) {}
        }
    }

    private static void pageThroughValuesOnlyIndex(FaunaClient client, String indexName) throws Exception {
        /*
         * Read all the records of a values based index
         * Use a small'ish page size so that we can demonstrate a paging example.
         *
         * NOTE: after is inclusive of the value.
         */
        Optional<Value> dataPage = Optional.empty();
        Optional<Value> cursorPos = Optional.empty();
        Expr paginationExpr;

        int pageSize = 8;   // ridiculously small page size for example purposes only
        do {

            if (!cursorPos.isPresent()) {
                paginationExpr = Paginate(Match(Index(indexName))).size(Value(pageSize));
            } else {
                paginationExpr = Paginate(Match(Index(indexName))).after(cursorPos.get()).size(Value(pageSize));
            }

            Value result = client.query(
                    Map(
                            paginationExpr,
                            Lambda(Value("x"), Select(Value("data"), Get(Var("x"))))
                    )
            ).get();

            dataPage = result.getOptional(Field.at("data"));
            if (dataPage.isPresent()) {

                logger.info("Page Results: {}", toPrettyJson(dataPage.get()));
            }

            cursorPos = result.getOptional(Field.at("after"));
            if (cursorPos.isPresent()) {
                logger.info("After: {}", toPrettyJson(cursorPos.get()));
            }

        } while (cursorPos.isPresent());
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

        Expr categoryByIdIdx = Obj(
                "name", Value("category_by_id"),
                "source", Class(Value("categories")),
                "terms", Arr(Obj("field", Arr(Value("data"), Value("name")))),
                "values", Arr(Obj("field", Arr(Value("data"), Value("categoryID"))))
        );
        createIndex(client, categoryByIdIdx, true);

        Expr productsByCategoryIdIdx = Obj(
                "name", Value("products_by_category_id"),
                "source", Class(Value("products")),
                "terms", Arr(Obj("field", Arr(Value("data"), Value("categoryID"))))
        );
        createIndex(client, productsByCategoryIdIdx, true);

        pageThroughValuesOnlyIndex(client, "categories_all");

        //
        // Just to keep things neat and tidy, close the client connections
        //
        client.close();
        logger.info("Disconnected from FaunaDB");

        // add this at the end of execution to make things shut down nicely
        System.exit(0);
    }
}
