package com.xh.utils.mock.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: mock入参格式
 * @author: XDcherish
 * @create: 2020-10-21
 **/
@Data
public class MockRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //mock类型
    //0-复杂类型，例如xxxRequest，选取部分字段进行比较
    //1-基本类型，例如Integer，String，List<String>，Map<Integer, Object>等，直接全部比较
    private Integer requestType = 0;   //默认为复杂类型

    //复杂类型时，Object为Map<String, Object>类型；
    //基本类型时，Object不固定；
    private Object requestCompareContent;
}