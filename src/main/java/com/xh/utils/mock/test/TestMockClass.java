package com.xh.utils.mock.test;

import com.xh.utils.mock.annotation.MethodMock;
import com.xh.utils.mock.test.dto.MockTestOutputDTO;
import com.xh.utils.mock.test.request.MockTestRequest;
import com.xh.utils.mock.test.response.MockTestResponse;
import com.xh.utils.mock.util.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@MethodMock
public class TestMockClass {

    public Boolean testSimpleWithoutInput() {
        System.out.println("真实执行啦:");
        return true;
    }

    public Boolean testSimpleInputOutput(String input) {
        System.out.println("真实执行啦:" + input);
        return false;
    }

    public MockTestResponse testComplexInputOutput(String requestFirst, MockTestRequest requestSecond) {
        MockTestResponse response = new MockTestResponse();
        response.setResId(9L);
        response.setResName("真实调用");
        response.setOutputDTOS(new ArrayList<>());
        MockTestOutputDTO outputDTO = new MockTestOutputDTO();
        outputDTO.setResSubId(901L);
        outputDTO.setResSubName("真实调用subName");
        response.setOutputDTO(outputDTO);
        System.out.println("真实执行啦:" + JsonUtils.toJson(response));
        return response;
    }

}
