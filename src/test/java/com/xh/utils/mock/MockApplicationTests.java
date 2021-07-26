package com.xh.utils.mock;

import com.xh.utils.mock.test.TestMockClass;
import com.xh.utils.mock.test.dto.MockTestInputDTO;
import com.xh.utils.mock.test.request.MockTestRequest;
import com.xh.utils.mock.test.response.MockTestResponse;
import com.xh.utils.mock.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MockApplicationTests {

    @Autowired
    private TestMockClass mockClass;

    @Test
    void contextLoads() {
        //  mockClass.testSimpleInputOutput("mock");
        //  mockClass.testSimpleWithoutInput();
        MockTestRequest request = new MockTestRequest();
        request.setId(1L);
        MockTestInputDTO inputDTO = new MockTestInputDTO();
        inputDTO.setSubId(101L);
        request.setInputDTO(inputDTO);
        MockTestResponse response = mockClass.testComplexInputOutput("1", request);
        System.out.println("最后的结果:" + JsonUtils.toJson(response));
    }

}
