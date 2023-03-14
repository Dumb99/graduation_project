package com.baiyun.asstorage.service.impl;

import com.baiyun.asstorage.dto.AsNeighborDTO;
import com.baiyun.asstorage.dto.AsPathDTO;
import com.baiyun.asstorage.mapper.AsNeighborMapper;
import com.baiyun.asstorage.mapper.AsPathMapper;
import com.baiyun.asstorage.service.AsNeighborService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AsNeighborServiceImpl implements AsNeighborService {

    @Resource
    private AsPathMapper asPathMapper;

    @Autowired
    private Environment env;

    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Resource
    private AsNeighborMapper asNeighborMapper;

    public static Logger logger = LoggerFactory.getLogger(AsNeighborServiceImpl.class);
    private List<Long> statusIds = new ArrayList<>();
    //计数器
    private static Integer HANDLE_COUNT = 0;
    private static boolean FLAG = Boolean.TRUE;

    @Override
    public String setFalg(boolean flag) {
        FLAG = flag;
        return String.valueOf(FLAG);
    }

    @Override
    public void buildAsNeigh() {
        logger.info("Insert data into the redis!");
        while (FLAG) {
            List<AsPathDTO> asPathDTOList = asPathMapper.selectByStatus();
            FLAG = !asPathDTOList.isEmpty();
            if (++HANDLE_COUNT % 20 == 0) {
                insertDBFromRedis();
                sendCommoond("flushall");
                logger.info("Flush then insert data into the redis!");
            }
            asPathDTOList.forEach(asPathDTO -> {
                String[] asPaths = asPathDTO.getAsPath().split(" ");
                isertAsNeighToRedis(asPaths);
                statusIds.add(asPathDTO.getId());
                if (statusIds.size() >= 1000) {
                    asPathMapper.updateStatusById(statusIds);
                    statusIds = new ArrayList<>();
                }
            });
        }
        insertDBFromRedis();
        logger.info("{} pieces of data have been processed！", HANDLE_COUNT);
    }

    @Override
    public Integer getHandleCount() {
        return HANDLE_COUNT;
    }

    @Override
    public String updateLoc() {
        List<String> asList = new ArrayList<>();
        String filePath = "/usr/logs/basic_as/as_loc.txt";
        File file = new File(filePath);
        BufferedReader bufferedReader = null;
        try {
            String as;
            bufferedReader = new BufferedReader(new FileReader(file));
            while (!Objects.isNull(as = bufferedReader.readLine())) {
                asList.add(as);
            }
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                logger.error("BufferedReader close failed:{}", e.toString());
            }
        }
        asNeighborMapper.updateLocation(asList);
        return "Update location has been finished!";
    }

    @Override
    public void businessRelationship() {
        String filePath = "/usr/logs/basic_as/20220401.as-rel2.txt";
        int count = 0;
        File file = new File(filePath);
        BufferedReader bufferedReader = null;
        String br;
        Map<String, String> p2p = new HashMap<>();
        Map<String, String> p2c = new HashMap<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            while (!Objects.isNull(br = bufferedReader.readLine())) {
                count ++;
                String[] str = br.split("\\|");
                String p = str[1];
                if ("0".equals(str[2])) {
                    if (p2p.containsKey(str[0])) {
                        p = p2p.get(str[0]) + " " + p;
                    }
                    p2p.put(str[0], p);
                } else {
                    if (p2c.containsKey(str[0])) {
                        p = p2c.get(str[0]) + " " + p;
                    }
                    p2c.put(str[0], p);
                }
                if (count%5000 == 0){
                    if (!p2p.isEmpty()){
                        Map<String, String> map = p2p;
                        List<AsNeighborDTO> asNeighborDTOS = asNeighborMapper.selectBatchByKeys(p2p.keySet());
                        asNeighborDTOS.forEach(asNeighborDTO -> {
                            if (!Objects.isNull(asNeighborDTO.getP2p())){
                                map.put(asNeighborDTO.getKeyAs(), map.get(asNeighborDTO.getKeyAs()) + " " + asNeighborDTO.getP2p());
                            }});
                        asNeighborMapper.updateP2p(map);
                        p2p = new HashMap<>();
                    }
                    if (!p2c.isEmpty()){
                        Map<String, String> map = p2c;
                        List<AsNeighborDTO> asNeighborDTOS = asNeighborMapper.selectBatchByKeys(p2c.keySet());
                        asNeighborDTOS.forEach(asNeighborDTO -> {
                            if (!Objects.isNull(asNeighborDTO.getP2c())){
                                map.put(asNeighborDTO.getKeyAs(), map.get(asNeighborDTO.getKeyAs()) + " " + asNeighborDTO.getP2c());
                            }});
                        asNeighborMapper.updateP2c(map);
                        p2c = new HashMap<>();
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                logger.error("BufferedReader close failed:{}", e.toString());
            }
        }
    }

    /**
     * 存入redis
     *
     * @param asPaths
     */
    private void isertAsNeighToRedis(String[] asPaths) {
        if (asPaths.length < 2) {
            return;
        }
        for (int i = 0; i < asPaths.length; i++) {
            String keyAs = asPaths[i];
            if (keyAs.length() > 6 || keyAs.length() < 4) {
                continue;
            }
            BoundSetOperations<String, Integer> setOps = redisTemplate.boundSetOps(keyAs);
            try {
                if (i == 0) {
                    setOps.add(Integer.valueOf(asPaths[1]));
                } else if (i == asPaths.length - 1) {
                    setOps.add(Integer.valueOf(asPaths[i - 1]));
                } else {
                    setOps.add(Integer.valueOf(asPaths[i - 1]), Integer.valueOf(asPaths[i + 1]));
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * redis -> mysql
     */
    private void insertDBFromRedis() {
        Set<String> keys = redisTemplate.keys("*");
        assert keys != null;
        if (keys.size() <= 0) {
            return;
        }
        int count = 0;

        Map<String, Set<String>> map = new HashMap<>(1000);
        for (String key : keys) {
            map.put(key, Objects.requireNonNull(redisTemplate.boundSetOps(key).members()).stream()
                    .map(String::valueOf).collect(Collectors.toSet()));
            if (++count % 1000 == 0) {
                insertToDB(map);
                map = new HashMap<>(1000);
            }
        }
        insertToDB(map);
        logger.info("{} neighbor as have been update to mysql!", keys.size());
    }

    private void insertToDB(Map<String, Set<String>> map) {
        Map<String, Set<String>> keyNeighborMap;
        List<AsNeighborDTO> updateList = new ArrayList<>();
        keyNeighborMap = asNeighborMapper.selectBatchByKeys(map.keySet()).stream()
                .collect(Collectors.toMap(AsNeighborDTO::getKeyAs, AsNeighborDTO::getNeighborSet));
        keyNeighborMap.forEach((key, value) -> {
            value.addAll(map.get(key));
            map.remove(key);
            updateList.add(new AsNeighborDTO(key, value));
        });
        if (!updateList.isEmpty()) {
            asNeighborMapper.updateBatchAsNeighbor(updateList);
        }
        List<AsNeighborDTO> insertList = new ArrayList<>();
        if (!map.isEmpty()) {
            map.forEach((key, value) -> {
                insertList.add(new AsNeighborDTO(key, value));
            });
            if (!insertList.isEmpty()) {
                asNeighborMapper.saveBatchAsNeighbor(insertList);
            }
        }
    }

    private String sendCommoond(String commond) {
        String[] commonds = commond.split(" ");

        StringBuilder cmd = new StringBuilder();
        cmd.append("*").append(commonds.length).append("\r\n");

        Arrays.stream(commonds).forEach(com -> {
            cmd.append("$").append(com.length()).append("\r\n");
            cmd.append(com).append("\r\n");
        });

        byte[] bytes = new byte[1024];
        try {
            String host = env.getProperty("spring.redis.host");
            Socket socket = new Socket(host, 6379);
            socket.getOutputStream().write(cmd.toString().getBytes());
            int res = socket.getInputStream().read();
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return Arrays.toString(bytes);
    }
}
