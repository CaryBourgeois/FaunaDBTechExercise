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

public class Product {
    static final Field<Product> PRODUCT_FIELD = Field.at("data").to(Product.class);

    @FaunaField private int productID;
    @FaunaField private int supplierID;
    @FaunaField private int categoryID;
    @FaunaField private String quantityPerUnit;
    @FaunaField private int unitPrice;
    @FaunaField private int unitsInStock;
    @FaunaField private int unitsOnOrder;
    @FaunaField private int reorderLevel;
    @FaunaField private boolean discontinued;
    @FaunaField private String name;

    @FaunaConstructor
    public Product (@FaunaField("productID") int productID,
                    @FaunaField("supplierID") int supplierID,
                    @FaunaField("categoryID") int categoryID,
                    @FaunaField("quantityPerUnit") String quantityPerUnit,
                    @FaunaField("unitPrice") int unitPrice,
                    @FaunaField("unitsInStock") int unitsInStock,
                    @FaunaField("unitsOnOrder") int unitsOnOrder,
                    @FaunaField("reorderLevel") int reorderLevel,
                    @FaunaField("discontinued") boolean discontinued,
                    @FaunaField("name") String name) {
        this.productID = productID;
        this.supplierID = supplierID;
        this.categoryID = categoryID;
        this.quantityPerUnit = quantityPerUnit;
        this.unitPrice = unitPrice;
        this.unitsInStock = unitsInStock;
        this.unitsOnOrder = unitsOnOrder;
        this.reorderLevel = reorderLevel;
        this.discontinued = discontinued;
        this.name = name;
    }

    public Product () {
        this.productID = 0;
        this.supplierID = 0;
        this.categoryID = 0;
        this.quantityPerUnit = "";
        this.unitPrice = 0;
        this.unitsInStock = 0;
        this.unitsOnOrder = 0;
        this.reorderLevel = 0;
        this.discontinued = true;
        this.name = "";
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(String quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getUnitsInStock() {
        return unitsInStock;
    }

    public void setUnitsInStock(int unitsInStock) {
        this.unitsInStock = unitsInStock;
    }

    public int getUnitsOnOrder() {
        return unitsOnOrder;
    }

    public void setUnitsOnOrder(int unitsOnOrder) {
        this.unitsOnOrder = unitsOnOrder;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public boolean isDiscontinued() {
        return discontinued;
    }

    public void setDiscontinued(boolean discontinued) {
        this.discontinued = discontinued;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

