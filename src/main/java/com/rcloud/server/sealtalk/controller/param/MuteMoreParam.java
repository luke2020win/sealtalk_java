package com.rcloud.server.sealtalk.controller.param;

import lombok.Data;

@Data
public class MuteMoreParam {
    private String groupId;
    private String[] memberIds;
}
