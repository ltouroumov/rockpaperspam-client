package com.tricktrap.rps.data;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class Friend {

    private String id;
    private boolean is_client;
    private String display_name;

    public Friend(String id, String displayName) {
        this.id = id;
        this.display_name = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String displayName) {
        this.display_name = displayName;
    }

    public boolean isClient() {
        return is_client;
    }

    public void setIsClient(boolean is_client) {
        this.is_client = is_client;
    }
}
