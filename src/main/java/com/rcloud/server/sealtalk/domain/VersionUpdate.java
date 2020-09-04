package com.rcloud.server.sealtalk.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "version_update")
public class VersionUpdate implements Serializable {

    public static Integer NO_SHOW_UPDATE = 0;
    public static Integer SHOW_UPDATE = 1;

    public static Integer NO_PLIST = 0;
    public static Integer PLIST = 1;

    public static Integer NO_FORCE = 0;
    public static Integer FOCRE = 1;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name="clientType")
    private String clientType;

    @Column(name="content")
    private String content;

    @Column(name="url")
    private String url;

    @Column(name="channel")
    private String channel;

    @Column(name="version")
    private String version;

    @Column(name="versionCode")
    private Integer versionCode;

    @Column(name="isForce")
    private Integer isForce;

    @Column(name="isShowUpdate")
    private Integer isShowUpdate;

    @Column(name="isPlist")
    private Integer isPlist;

    @Column(name="description")
    private String description;

    @Column(name="timestamp")
    private Long timestamp;

    @Column(name="createdAt")
    private Date createdAt;

    @Column(name="updatedAt")
    private Date updatedAt;

    @Column(name="deletedAt")
    private Date deletedAt;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getIsForce() {
        return isForce;
    }

    public void setIsForce(Integer isForce) {
        this.isForce = isForce;
    }

    public Integer getIsShowUpdate() {
        return isShowUpdate;
    }

    public void setIsShowUpdate(Integer isShowUpdate) {
        this.isShowUpdate = isShowUpdate;
    }

    public Integer getIsPlist() {
        return isPlist;
    }

    public void setIsPlist(Integer isPlist) {
        this.isPlist = isPlist;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
