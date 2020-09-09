package com.rcloud.server.sealtalk.model.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
public class GroupMuteDTO implements Serializable {

    private Integer groupId;

    private Integer muteUserId;

    private String muteNickname;

    private String mutePortraitUri;

    private Integer operatorId;

    private String operatorNickName;
}
