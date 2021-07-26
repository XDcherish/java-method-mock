package com.xh.utils.mock.test.response;

import com.xh.utils.mock.test.dto.MockTestOutputDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MockTestResponse implements Serializable {

    private Long resId;

    private String resName;

    private List<MockTestOutputDTO> outputDTOS;

    private MockTestOutputDTO outputDTO;
}
