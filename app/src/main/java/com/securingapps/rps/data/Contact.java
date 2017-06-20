package com.securingapps.rps.data;

import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ldavid
 * @created 3/17/17
 */
public class Contact {

    public static int PREF_PROFILE_ID = -1;

    private long id;
    private String key;
    private String name;
    private List<Raw> rawContacts = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Uri getUri() {
        return ContactsContract.Contacts.getLookupUri(id, key);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Raw> getRawContacts() {
        return rawContacts;
    }

    public Raw getRawContact(long id) {
        for (Raw raw : this.rawContacts) {
            if (raw.getId() == id) return raw;
        }
        return null;
    }

    public Raw getRawContact(String type) {
        for (Raw raw : this.rawContacts) {
            if (raw.getType().equals(type)) return raw;
        }
        return null;
    }

    public List<Data> getData() {
        List<Data> data = new ArrayList<>();
        for (Raw raw : rawContacts) {
            data.addAll(raw.getData());
        }
        return data;
    }

    public List<Data> get(DataType type) {
        List<Data> subset = new ArrayList<>();
        for (Raw raw : rawContacts) {
            subset.addAll(raw.get(type));
        }

        return subset;
    }

    public List<String> getValues(DataType type) {
        List<String> subset = new ArrayList<>();
        for (Raw raw : rawContacts) {
            subset.addAll(raw.getValues(type));
        }
        return subset;
    }

    public Data getFirst(DataType type) {
        for (Raw raw : rawContacts) {
            Data item = raw.getFirst(type);
            if (item != null) return item;
        }
        return null;
    }

    public String getFirstValue(DataType type) {
        for (Raw raw : rawContacts) {
            Data item = raw.getFirst(type);
            if (item != null) return item.getValue();
        }
        return null;
    }

    public boolean isCoherent() {
        return getId() != 0 && getKey() != null && getName() != null;

    }

    public static class Raw {

        private long id;
        private String type;
        private String name;

        private List<Data> data = new ArrayList<>();

        public Raw(long id, String type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public List<Data> getData() {
            return data;
        }

        public List<Data> get(DataType type) {
            List<Data> subset = new ArrayList<>();
            for (Data item : this.data) {
                if (item.type == type) {
                    subset.add(item);
                }
            }

            return subset;
        }

        public List<String> getValues(DataType type) {
            List<String> subset = new ArrayList<>();
            for (Data item : this.data) {
                if (item.type == type) {
                    subset.add(item.getValue());
                }
            }
            return subset;
        }

        public Data getFirst(DataType type) {
            for (Data item : this.data) {
                if (item.type == type) {
                    return item;
                }
            }
            return null;
        }

        public String getFirstValue(DataType type) {
            for (Data item : this.data) {
                if (item.type == type) {
                    return item.getValue();
                }
            }
            return null;
        }

    }

    public static class Data {

        private DataType type;
        private String value;

        public Data(DataType type, String value) {
            this.type = type;
            this.value = value;
        }

        public DataType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

    }

    public enum DataType {
        NAME, PHONE, EMAIL, WHATSAPP
    }

}
