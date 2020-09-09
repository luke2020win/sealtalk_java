package com.rcloud.server.sealtalk.domain;

import io.swagger.models.auth.In;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "group_mute_lists")
public class GroupMute implements Serializable {

    @Id
    private Integer id;

    @Column(name = "groupId")
    private Integer groupId;

    @Column(name = "muteUserId")
    private Integer muteUserId;

    @Column(name = "muteNickname")
    private String muteNickname;

    @Column(name = "mutePortraitUri")
    private String mutePortraitUri;

    @Column(name = "muteTime")
    private Integer muteTime;

    @Column(name = "operatorId")
    private Integer operatorId;

    @Column(name = "operatorNickName")
    private String operatorNickName;

    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getMuteUserId() {
        return muteUserId;
    }

    public void setMuteUserId(Integer muteUserId) {
        this.muteUserId = muteUserId;
    }

    public String getMuteNickname() {
        return muteNickname;
    }

    public void setMuteNickname(String muteNickname) {
        this.muteNickname = muteNickname;
    }

    public String getMutePortraitUri() {
        return mutePortraitUri;
    }

    public void setMutePortraitUri(String mutePortraitUri) {
        this.mutePortraitUri = mutePortraitUri;
    }

    public Integer getMuteTime() {
        return muteTime;
    }

    public void setMuteTime(Integer muteTime) {
        this.muteTime = muteTime;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorNickName() {
        return operatorNickName;
    }

    public void setOperatorNickName(String operatorNickName) {
        this.operatorNickName = operatorNickName;
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
}
