package com.lu.luaicode.common;


import lombok.Data;


import java.io.Serializable;


/**
 * 分页结果封装类
 * 泛型T表示列表中元素的类型
 * 实现Serializable接口以支持序列化
 */
@Data
public class PageRequest<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNum=1;    // 当前页码
    private int pageSize=10;   // 每页大小
    private String sortField; // 排序字段
    private String sortOrder="descend"; // 排序顺序


}
