package com.xh.utils.mock.test.request;

import com.xh.utils.mock.test.dto.MockTestInputDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class MockTestRequest implements Serializable {

    private Long id;

    private String name;

    private MockTestInputDTO inputDTO;
}
