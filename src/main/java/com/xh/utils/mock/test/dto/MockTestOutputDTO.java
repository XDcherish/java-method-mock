package com.xh.utils.mock.test.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MockTestOutputDTO implements Serializable {

    private Long resSubId;

    private String resSubName;
}
