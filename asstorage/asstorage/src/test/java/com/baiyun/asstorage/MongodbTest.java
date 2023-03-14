package com.baiyun.asstorage;

import com.baiyun.asstorage.service.PortalService;
import com.baiyun.asstorage.vo.CheckVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class MongodbTest {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private PortalService portalService;

    @Test
    public void insert(){
        String asPath = "6447 10073 8989";
        CheckVO checkVO = portalService.singleCheck(asPath,"");
//        checkVO.setTime(TimeHelper.getNowTime());
//        CheckVO insert = mongoTemplate.insert(checkVO);
//
//        System.out.println(insert.getTime());
        System.out.println();
    }

    @Test
    public void findPage(){
        int pageNo = 1;
        int pageSize = 10;
        Query query = new Query(Criteria.where("email").is(""));
        List<CheckVO> checkVOS = mongoTemplate.find(query.skip((pageNo - 1) * pageSize).limit(10), CheckVO.class);
    }
}
