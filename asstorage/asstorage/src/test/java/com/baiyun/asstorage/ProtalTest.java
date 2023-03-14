package com.baiyun.asstorage;

import com.baiyun.asstorage.service.PortalService;
import com.baiyun.asstorage.vo.CheckVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class ProtalTest {

    @Resource
    private PortalService portalService;

    @Test
    public void checkAsPath(){
        //6447,52873,12956,2914,12956,9304,55334
        // 6447 52873 12956 2914 9304 55334
        String asPath = "50300 1828 131477 4800 132203";
        CheckVO checkVO = portalService.singleCheck(asPath);
        assert checkVO.isSuccess();
    }
}
