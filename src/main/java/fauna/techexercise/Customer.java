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

public class Customer {
    static final Field<Customer> CUSTOMER_FIELD = Field.at("data").to(Customer.class);

    @FaunaField private String customerId;
    @FaunaField private String companyName;
    @FaunaField private String contactName;
    @FaunaField private String contactTitle;
    @FaunaField private Address address;

    @FaunaConstructor
    public Customer(@FaunaField("customerId") String customerId,
                    @FaunaField("companyName") String companyName,
                    @FaunaField("contactName") String contactName,
                    @FaunaField("contactTitle") String contactTitle,
                    @FaunaField("address") Address address) {
        this.customerId = customerId;
        this.companyName = companyName;
        this.contactName = contactName;
        this.contactTitle = contactTitle;
        this.address = address;
    }

    public Customer () {
        this.customerId = "";
        this.companyName = "";
        this.contactName = "";
        this.contactTitle = "";
        this.address = new Address();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactTitle() {
        return contactTitle;
    }

    public void setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
