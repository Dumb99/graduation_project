package com.baiyun.asstorage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AsstorageApplicationTests {

    @Test
    void contextLoads() {
        String as = "58308 6939   3356 20473";
        String s = as.replaceAll("\\s", ",")
                .replaceAll(",+", ",");
        System.out.println(s);
    }

}
