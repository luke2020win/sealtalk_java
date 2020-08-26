package com.rcloud.server.sealtalk.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "backend_system_config")
public class BackendSystemConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name="varName")
    private String varName;

    @Column(name="varValue")
    private String varValue;

    @Column(name="varDes")
    private String varDes;

    @Column(name="timestamp")
    private Long timestamp;

    @Column(name="createdAt")
    private Date createdAt;

    @Column(name="updatedAt")
    private Date updatedAt;

    @Column(name="deletedAt")
    private Date deletedAt;
}
