package com.xh.utils.mock.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: mock出参格式
 * @author: XDcherish
 * @create: 2020-10-21
 **/
@Data
public class MockResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //mock类型
    //0-修改类型，真实调用之后修改部分字段结果
    //1-构造类型，mock时返回整个response
    private Integer mockType = 0;   //默认为修改类型

    //修改类型时注意键值对的value需要都为String类型，构造类型不需要
    private Object responseContent;
}
