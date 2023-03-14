package com.baiyun.asstorage;

import com.baiyun.asstorage.helper.JwtHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.Socket;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Autowired
    private Environment env;

    @Test
    public void tokenTest(){
        String baiyun = JwtHelper.createToken("1621043603@qq.com", "baiyun");
        System.out.println(baiyun);
    }

    @Test
    public void redisTemplateTest(){
//        List<Integer> list = new ArrayList<>();
//        list.add(62313);
//        list.add(33641);
//        list.add(9456);
//        redisTemplate.boundSetOps("10929").add(5016,5103,65372);
//        redisTemplate.boundSetOps("10929").add(5017,5103,65372);
//        Set<Object> members = redisTemplate.boundSetOps("10929").members();
        System.out.println();
//        redisTemplate.opsForSet().add(45498, 62313,33641,9456);
//        redisTemplate.delete("10929");
//        keys("*").forEach(System.out::println);
        System.out.println(redisTemplate.keys("*"));
    }

    @Test
    public void selfMadeRedis() throws IOException {
        Socket socket = new Socket("127.0.0.1", 6379);

        String key = "929";
        String value = "10929";

        StringBuilder cmd = new StringBuilder();
        cmd.append("*1").append("\r\n");

        //第一部分的数据长度
        cmd.append("$8").append("\r\n");
        //第一部分的数据
        cmd.append("flushall").append("\r\n");


        socket.getOutputStream().write(cmd.toString().getBytes());
    }

    @Test
    public void envTest(){
        System.out.println(env.getProperty("spring.redis.host"));
    }

    @Test
    public void RedisTemplateTest() {

        redisTemplate.boundSetOps("10929").add(5016);

    }
}
