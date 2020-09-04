package com.rcloud.server.sealtalk.controller.param;

import lombok.Data;

@Data
public class ClientVersionParam {
    private String channel;
    private String version;
    private String clientType;
}
