package com.rcloud.server.sealtalk.controller.param;

import com.rcloud.server.sealtalk.domain.BackendUsers;

import java.util.List;

public class PageBeanRes<T> {
    // 当前页,从请求那边传过来。
    private int page;
    // 每页显示的数据条数。
    private int pageSize;
    // 每页显示的数据条数。
    private int total;

    private List<T> data;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
