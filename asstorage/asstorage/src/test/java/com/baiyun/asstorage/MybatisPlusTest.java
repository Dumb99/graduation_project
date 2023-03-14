package com.baiyun.asstorage;

import com.baiyun.asstorage.dto.AsNeighborDTO;
import com.baiyun.asstorage.dto.UserDTO;
import com.baiyun.asstorage.mapper.AsNeighborMapper;
import com.baiyun.asstorage.mapper.AsPathMapper;
import com.baiyun.asstorage.mapper.UserMapper;
import com.baiyun.asstorage.service.AsPathService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
public class MybatisPlusTest {

    @Resource
    private AsPathMapper asPathMapper;

    @Resource
    private AsNeighborMapper asNeighborMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AsPathService asPathService;

//    public static Logger logger = LoggerFactory.getLogger(MybatisPlusTest.class);

    @Test
    public void testSelectList(){
        Map<String, String> map = new HashMap<>();
        map.put("10029","test3");
        map.put("10075","test4");
        asNeighborMapper.updateP2c(map);
        System.out.println("1");
    }

    @Test
    public void NioTest() throws IOException {
        long l = System.currentTimeMillis();
        asPathService.insertAsPathDto("/usr/logs/basic_as/as_collect.03.23");
        System.out.println("useTime:" + (System.currentTimeMillis()-l)+"ms");
    }

    @Test
    public void xmlTest(){
        asPathMapper.selectByStatus().forEach(asPathDTO -> {
//                  asPathMapper.updateStatusById(asPathDTO.getId());
        });
    }

    @Test
    public void neighborTest(){
        List<AsNeighborDTO> list = new ArrayList<>(100);
        Set<String> set = new HashSet<>();
        set.add("test0");
        set.add("test1");
        set.add("test2");
        for (int i = 0; i < 100; i++) {
            AsNeighborDTO asNeighborDTO = new AsNeighborDTO(String.valueOf(i+1),set);
            list.add(asNeighborDTO);
        }
        asNeighborMapper.saveBatchAsNeighbor(list);
        System.out.println("H");
    }

    @Test
    public void updateTest(){
        List<AsNeighborDTO> list = new ArrayList<>();
        list.add(new AsNeighborDTO("1","111"));
        list.add(new AsNeighborDTO("10","101010"));
        asNeighborMapper.updateBatchAsNeighbor(list);
    }
    @Test
    public void addNeighborTest(){
        Set<String> set = new HashSet<>(100);
        for (int i = 0; i < 100; i++) {
            set.add(String.valueOf(i+1));
        }
        asNeighborMapper.selectBatchByKeys(set).forEach(asNeighborDTO -> {
            System.out.println(asNeighborDTO.getKeyAs()+" :: " +asNeighborDTO.getNhbAs());
        });
//        AsNeighborDTO asNeighborDTO = asNeighborMapper.selectByKey("1");
        System.out.println("asNeighborDTO.getKeyAs()+asNeighborDTO.getNhbAs()");
    }
    @Test
    public void user(){
        UserDTO userDTO = userMapper.selectByEmail("1");
        System.out.println();
    }
    @Test
    public void find(){
        String[] s = {"10212", "10122", "131477"};//, "132203", "133111","133527", "134238"
        for (String s1 : s) {
            AsNeighborDTO asNeighborDTO = asNeighborMapper.selectByKey(s1);
            Set<AsNeighborDTO> collect = asNeighborMapper.selectBatchByKeys(asNeighborDTO.getNeighborSet())
                    .stream().filter(AsNeighborDTO::outNation).collect(Collectors.toSet());
            collect.forEach(e->{
                Set<String> nationSet = asNeighborMapper.selectBatchByKeys(e.getNeighborSet()).stream()
                        .filter(AsNeighborDTO::inNation).map(AsNeighborDTO::getKeyAs).collect(Collectors.toSet());
                if (!nationSet.isEmpty()){
                    nationSet.forEach(n -> {
                        if (!n.equals(s1)){
                            System.out.println(s1 + " " + e.getKeyAs() + " " + n);
                        }
                    });
                }
            });
        }
    }
}