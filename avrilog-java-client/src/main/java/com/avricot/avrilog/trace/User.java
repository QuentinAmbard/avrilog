package com.avricot.avrilog.trace;

import org.msgpack.annotation.Message;

@Message
public class User {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String groupId;
    private String groupName;
    private String ip;

    public User(final String id, final String firstname, final String lastname, final String email, final String groupId, final String ip) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.groupId = groupId;
        this.ip = ip;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public User setId(final String id) {
        this.id = id;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public User setFirstname(final String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public User setLastname(final String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(final String email) {
        this.email = email;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public User setGroupId(final String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public User setIp(final String ip) {
        this.ip = ip;
        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public User setGroupName(final String groupName) {
        this.groupName = groupName;
        return this;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + ", email=" + email + ", groupId=" + groupId + ", ip=" + ip + "]";
    }
}
