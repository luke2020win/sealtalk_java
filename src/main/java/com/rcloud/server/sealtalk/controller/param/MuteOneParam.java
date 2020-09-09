package com.rcloud.server.sealtalk.controller.param;

import lombok.Data;

@Data
public class MuteOneParam {
    private String groupId;
    private String memberId;
    private int minute;
}
