package com.avricot.avrilog.trace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.annotation.Message;

import com.avricot.avrilog.IdGenerator;

@Message
public class Trace {
    private byte[] id;
    private String applicationName;
    private String entityId;
    private String category;
    private String info;
    private long clientDate = System.currentTimeMillis();
    boolean sign = false;
    boolean horodate = false;
    private User user;
    private Map<String, String> data = null;

    public Trace() {
        id = IdGenerator.generateId();
    }

    public Trace(final String info) {
        this();
        this.info = info;
    }

    public byte[] getId() {
        return id;
    }

    public Trace setId(final byte[] id) {
        this.id = id;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public Trace setCategory(final String category) {
        this.category = category;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public Trace setInfo(final String info) {
        this.info = info;
        return this;
    }

    public long getClientDate() {
        return clientDate;
    }

    public Trace setClientDate(final long clientDate) {
        this.clientDate = clientDate;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Trace setUser(final User user) {
        this.user = user;
        return this;
    }

    public Map<String, String> getData() {
        return data;
    }

    public Trace addData(final String key, final String value) {
        if (this.data == null) {
            this.data = new HashMap<String, String>();
        }
        this.data.put(key, value);
        return this;
    }

    public Trace setData(final Map<String, String> data) {
        this.data = data;
        return this;
    }

    public boolean isSign() {
        return sign;
    }

    public Trace setSign(final boolean sign) {
        this.sign = sign;
        return this;
    }

    public boolean isHorodate() {
        return horodate;
    }

    public Trace setHorodate(final boolean horodate) {
        this.horodate = horodate;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public Trace setEntityId(final String entityId) {
        this.entityId = entityId;
        return this;
    }

    public Trace setEntityId(final Long entityId) {
        this.entityId = entityId.toString();
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Trace setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    @Override
    public String toString() {
        return "Trace [category=" + category + ", info=" + info + ", clientDate=" + clientDate + ", user=" + user + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(id);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Trace other = (Trace) obj;
        if (!Arrays.equals(id, other.id)) {
            return false;
        }
        return true;
    }
}
