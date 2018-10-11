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

import com.faunadb.client.types.FaunaConstructor;
import com.faunadb.client.types.FaunaField;
import com.faunadb.client.types.Field;

public class Category {
    static final Field<Category> CATEGORY_FIELD = Field.at("data").to(Category.class);

    @FaunaField private int categoryID;
    @FaunaField private String description;
    @FaunaField private String name;

    @FaunaConstructor
    public Category (@FaunaField("categoryID") int categoryID,
                   @FaunaField("description") String description,
                   @FaunaField("name") String name) {
        this.categoryID = categoryID;
        this.description = description;
        this.name = name;
    }

    public Category () {
        this.categoryID = 0;
        this.description = "";
        this.name = "";
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
