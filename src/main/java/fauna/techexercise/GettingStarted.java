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
 *  This part of the exercise can be accomplished with this code or
 *  by using the fauna command line client.  The command line client can be
 *  located here: https://www.npmjs.com/package/fauna-shell. Alternatively if
 *  you are a Mac user, you can install it using brew with the command:
 *  `brew install fauna-shell`.
 *
 *  In general is is considered bac practice to store data in the root of your
 *  fauna account. So below we are going to connect to the fauna root and then
 *  create a child database and get a new access key specific to it.
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

public class GettingStarted {

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

    public static void main(String[] args)  throws Exception {
        /*
         * Create an admin connection to FaunaDB.
         *
         * If you are using the FaunaDB-Cloud version:
         *  - the 'withEndpoint' line below is optional
         *  - substitute your secret for "secret" below
         */
        FaunaClient adminClient = FaunaClient.builder()
                .withEndpoint("https://db.fauna.com")
                .withSecret("fnAC86vPkUACDHV_whs3sTHWG-xL7m9rPc8OK6Ik")
                .build();
        logger.info("Succesfully connected to FaunaDB as Admin!");

        /*
         * The code below creates the Database that will be used for this example. Please note that
         * the existence of the database is evaluated, deleted if it exists and recreated with a single
         * call to the Fauna DB.
         */
        String dbName = "Northwind";

        Value result = adminClient.query(
                Arr(
                        If(
                                Exists(Database(dbName)),
                                Delete(Database(dbName)),
                                Value(true)
                        ),
                        CreateDatabase(Obj("name", Value(dbName)))
                )
        ).get();
        logger.info("Created database: {} :: \n{}", dbName, toPrettyJson(result));

        /*
         * Create a key specific to the database we just created. We will use this to
         * create a new client we will use in the remainder of the exercise.
         */
        result = adminClient.query(
                CreateKey(Obj("database", Database(Value(dbName)), "role", Value("admin")))
        ).get();
        String secret = result.at("secret").to(String.class).get();
        logger.info("DB {} secret: {}", dbName, secret);


        //
        // Just to keep things neat and tidy, close the client connections
        //
        adminClient.close();
        logger.info("Disconnected from FaunaDB");

        // add this at the end of execution to make things shut down nicely
        System.exit(0);
    }
}
