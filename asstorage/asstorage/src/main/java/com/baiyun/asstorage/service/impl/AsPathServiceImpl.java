package com.baiyun.asstorage.service.impl;

import com.baiyun.asstorage.dto.AsPathDTO;
import com.baiyun.asstorage.mapper.AsPathMapper;
import com.baiyun.asstorage.service.AsPathService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class AsPathServiceImpl implements AsPathService {

    @Resource
    private AsPathMapper asPathMapper;

    @Autowired
    private Environment env;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static Integer HANDLE_COUNT = 0;
    private static boolean FLAG = Boolean.TRUE;

    public static Logger logger = LoggerFactory.getLogger(AsPathServiceImpl.class);

    ExecutorService executorService = new ThreadPoolExecutor(
            4,                 //corePoolSize
            100,                //maximumPoolSize
            5L,                //keepAliveSize
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void insertAsPathDto(String filePath) throws IOException {
        logger.info("Start inserting files({}) into the database", filePath.split("/")[4]);
        long startTimes = System.currentTimeMillis();
        File file = new File(filePath);
        FileChannel fileChannel = new RandomAccessFile(file,"r").getChannel();
        long fileSize = fileChannel.size();
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byte[] temp = new byte[0];
        while(fileChannel.read(byteBuffer) != -1) {
            byte[] bs = new byte[byteBuffer.position()];
            byteBuffer.flip();
            byteBuffer.get(bs);
            byteBuffer.clear();
            int startNum=0;
            boolean isNewLine = false;
            for(int i=0;i < bs.length;i++) {
                if(bs[i] == 10) {
                    isNewLine = true;
                    startNum = i;
                }
            }

            if(isNewLine) {
                byte[] toTemp = new byte[temp.length+startNum];
                System.arraycopy(temp,0,toTemp,0,temp.length);
                System.arraycopy(bs,0,toTemp,temp.length,startNum);
                AsPathDTO asPathDTO = buildAsPath(new String(temp)+"BY");
                executorService.submit(() -> {
                    asPathMapper.insert(asPathDTO);
                });
                temp = new byte[bs.length-startNum-1];
                System.arraycopy(bs,startNum+1,temp,0,bs.length-startNum-1);
            } else {
                byte[] toTemp = new byte[temp.length + bs.length];
                System.arraycopy(temp, 0, toTemp, 0, temp.length);
                System.arraycopy(bs, 0, toTemp, temp.length, bs.length);
                temp = toTemp;
            }

        }
        AsPathDTO asPathDTO = buildAsPath(new String(temp)+"BY");
        executorService.submit(() -> {
            asPathMapper.insert(asPathDTO);
        });
        long handleTime = System.currentTimeMillis() - startTimes;
        logger.info("fileName:{}, fileSize:{} ,insertTime:{}min", filePath.split("/")[4],
                resetFileSize(fileSize), String.format("%.2f", (double) handleTime /1000/60));
    }

    @Override
    public String statusCount() {
        Map map = asPathMapper.statusCount();
        logger.info("Status　count: {}", map);
        return map.toString();
    }


    private AsPathDTO buildAsPath(String asString) {
        AsPathDTO asPathDTO = new AsPathDTO();
        String[] bgpMsg = asString.split("\\|");
        if (bgpMsg.length < 13){
            return new AsPathDTO(-1);
        }
        for (int i = 0; i < bgpMsg.length; i++) {
            if ("None".equals(bgpMsg[i]) || StringUtils.isBlank(bgpMsg[i])){
                bgpMsg[i] = null;
            }
        }
        int i = 1;
        asPathDTO.setRecordType(bgpMsg[i++]);
        asPathDTO.setTime(bgpMsg[i++]);
        asPathDTO.setProject(bgpMsg[i++]);
        asPathDTO.setCollector(bgpMsg[i++]);
        asPathDTO.setRouter(bgpMsg[i++]);
        asPathDTO.setRouterIp(bgpMsg[i++]);
        asPathDTO.setPeerAsn(bgpMsg[i++]);
        asPathDTO.setPeerAddress(bgpMsg[i++]);
        asPathDTO.setPrefix(bgpMsg[i++]);
        asPathDTO.setNextHop(bgpMsg[i++]);
        asPathDTO.setAsPath(bgpMsg[i++]);
        asPathDTO.setCommunities(bgpMsg[i]);
        return asPathDTO;
    }

    /**
     * 文件单位转换
     * @param fileSize
     * @return
     */
    private String resetFileSize(Long fileSize){
        String[] unit = {"B","KB","MB","GB","TB"};
        int index = 0;
        double ans = fileSize.doubleValue();
        while(ans > 1024){
            ans /= 1024;
            index++;
        }
        return String.format("%.2f",ans)+unit[index];
    }

}
