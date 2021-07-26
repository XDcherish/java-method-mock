package com.xh.utils.mock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 不同方法的mock配置
 * @author: XDcherish
 * @create: 2020-10-21
 */
@Data
public class MethodMockDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //是否开启mock
    private Boolean openMock;

    //支持批量入参对应批量出参，其中mockRequestDTOsList中的一个List<MockRequestDTO>对应mockResponses中的一个MockResponseDTO
    private List<List<MockRequestDTO>> mockRequestDTOsList;

    //mock类型为0时返回的序列化结果集合
    private List<MockResponseDTO> mockResponseDTOS;

}


