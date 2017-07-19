package com.tricktrap.rps.data;

import java.util.ArrayList;

/**
 * @author ldavid
 * @created 3/24/17
 */
public class ContactCollection extends ArrayList<Contact> {

    public Contact find(SearchFunction<Contact> predicate) {
        for (Contact contact : this) {
            if (predicate.matches(contact))
                return contact;
        }
        return null;
    }

}
