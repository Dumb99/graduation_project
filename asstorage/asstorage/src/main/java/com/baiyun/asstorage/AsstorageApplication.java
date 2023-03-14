package com.baiyun.asstorage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baiyun.asstorage.mapper")
public class AsstorageApplication {

    public static void main(String[] args) {
        try{
            SpringApplication.run(AsstorageApplication.class, args);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
