package com.baiyun.asstorage;

import com.baiyun.asstorage.dto.AsNeighborDTO;
import com.baiyun.asstorage.mapper.AsNeighborMapper;
import com.baiyun.asstorage.service.AsNeighborService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
public class AsNeighborTest {

    @Resource
    private AsNeighborService asNeighborService;

    @Resource
    private AsNeighborMapper asNeighborMapper;

    private Map<String, Set<String>> picMap = new HashMap<>();

    @Test
    public void businessTest(){
        asNeighborService.businessRelationship();
        System.out.println("1");
    }

    @Test
    public void drawTop() throws IOException {
        FileOutputStream outputStream1 = new FileOutputStream("/home/baiyun/Documents/map.txt");
        FileOutputStream outputStream2 = new FileOutputStream("/home/baiyun/Documents/sp.txt");
        Set<String> keyAsSet = new HashSet<>();
        StringBuffer stringBuffer1 = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        String[] keyAs = {"7473","17639","24961","35426","47441","55222","61568"};
        keyAsSet.add("42");
        for (String key : keyAs) {
            stringBuffer1.append("\t\t\t{ from: 42, to: ").append(key).append(" },\n");
            keyAsSet.add(key);
            getPic(key,stringBuffer1,keyAsSet,0);
        }
        picMap.forEach((k,v) -> {
            v.forEach(str -> {
                stringBuffer1.append("\t\t\t{ from: ").append(k).append(", to: ").append(str).append(" },\n");
            });
        });
        for (String str : keyAsSet) {
            stringBuffer2.append("\t\t\t{ id: ").append(str).append(", label: '").append(str).append("' },\n");
        }
        outputStream1.write(stringBuffer1.toString().getBytes(StandardCharsets.UTF_8));
        outputStream2.write(stringBuffer2.toString().getBytes(StandardCharsets.UTF_8));
        outputStream1.close();
        outputStream2.close();
    }

    public void getPic(String keyAs, StringBuffer stringBuffer, Set<String> set, int count){
        if (count > 2) return;
//        System.out.println(keyAs);
        AsNeighborDTO asNeighborDTO = asNeighborMapper.selectByKey(keyAs);
        if (Objects.isNull(asNeighborDTO)) return;
        Set<String> neighborSet = asNeighborDTO.getNeighborSet();
        neighborSet.remove(keyAs);
        if (neighborSet.size()<1) return;
        int index = 0;
        for (String neighbor : neighborSet) {
            if (index > 5) break;
            if (neighbor.equals(keyAs)){
                continue;
            }
            setMap(keyAs,neighbor);
//            stringBuffer.append("\t\t\t{ from: ").append(keyAs).append(", to: ").append(neighbor).append(" },\n");
            set.add(neighbor);
            getPic(neighbor, stringBuffer, set, count+1);
            index++;
        }
    }

    public void setMap(String a, String b){
        int A = Integer.parseInt(a), B = Integer.parseInt(b);
        if (A < B){
            if (!picMap.containsKey(a)){
                picMap.put(a,new HashSet<>());
            }
            picMap.get(a).add(b);
        } else {
            if (!picMap.containsKey(b)){
                picMap.put(b,new HashSet<>());
            }
            picMap.get(b).add(a);
        }
    }
}
