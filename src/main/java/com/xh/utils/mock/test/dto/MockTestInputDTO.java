package com.xh.utils.mock.test.dto;

import lombok.Data;

import java.io.Serializable;

@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockTestInputDTO implements Serializable {

    private Long subId;

    private String subName;
}
