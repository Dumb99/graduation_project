package com.baiyun.asstorage.service.impl;

import com.baiyun.asstorage.dto.AsNeighborDTO;
import com.baiyun.asstorage.enu.ErrorResultEnum;
import com.baiyun.asstorage.helper.TimeHelper;
import com.baiyun.asstorage.mapper.AsNeighborMapper;
import com.baiyun.asstorage.service.PortalService;
import com.baiyun.asstorage.vo.CheckVO;
import com.baiyun.asstorage.vo.ErrorVO;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortalServiceImpl implements PortalService {

    public static Logger logger = LoggerFactory.getLogger(PortalServiceImpl.class);

    @Resource
    private AsNeighborMapper asNeighborMapper;

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public CheckVO singleCheck(String asPath) {
        return singleCheck(asPath, null);
    }

    @Override
    public CheckVO singleCheck(String asPath, String email) {
        CheckVO checkVO = checkAs(asPath, email);
        mongoTemplate.insert(checkVO);
        return checkVO;
    }

    private CheckVO checkAs(String asPath, String email) {
        if (Strings.isBlank(asPath)) {
            return new CheckVO(ErrorResultEnum.ILLEGAL_INPUT.getMsg());
        }
        String[] asSplit = asPath
                .replaceAll("\\s",",")
                .replaceAll(",+", ",")
                .replaceAll(";+", ";")
                .split("[,;]");
        if (asSplit.length < 2) {
            return new CheckVO(ErrorResultEnum.ILLEGAL_INPUT.getMsg());
        }
        for (String as : asSplit) {
            try {
                Integer.valueOf(as);
            } catch (Exception e) {
                return new CheckVO(ErrorResultEnum.ILLEGAL_INPUT.getMsg() + ": " + as);
            }
        }

        CheckVO checkVO = new CheckVO();
        ruleOneAndTwo(checkVO, asSplit);
        ruleThree(checkVO,asSplit);
        ruleFour(checkVO, asSplit);

        if (!checkVO.getErrorList().isEmpty()) {
            checkVO.setSuccess(false);
        }
        for (String s : asSplit) {
            checkVO.getAsPath().add(s);
        }
        checkVO.setEmail(email);
        checkVO.setTime(TimeHelper.getNowTime());
        return checkVO;
    }

    @Override
    public List<CheckVO> bactchCheck(MultipartFile file,String email) {
        List<CheckVO> checkVOList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String readLine;
            while(!Objects.isNull(readLine = bufferedReader.readLine())){
                CheckVO checkVO = checkAs(readLine,email);
                checkVO.setFilename(file.getOriginalFilename());
                checkVOList.add(checkVO);
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }
        mongoTemplate.insert(checkVOList,"checkHistory");
        return checkVOList;
    }

    /**
     * 1.AS-PATH中出现重复的AS
     * OR
     * 2.非连续重复AS
     *
     * @param checkVO
     */
    private void ruleOneAndTwo(CheckVO checkVO, String[] asSplit) {
        Set<Integer> repeatSet = cheakAsRepeat(asSplit);
        if (repeatSet.isEmpty()) {
            return;
        }
        Set<String> adjacentRepeat = new HashSet<>();
        Set<String> asLoop = new HashSet<>();
        int anchor = -1;
        for (Integer as : repeatSet) {
            if (anchor < 0) {
                anchor = as;
                continue;
            }
            if (anchor + 1 == as) {
                adjacentRepeat.add(asSplit[anchor]);
                adjacentRepeat.add(asSplit[as]);
            } else {
                asLoop.add(asSplit[anchor]);
                asLoop.add(asSplit[as]);
            }
        }
        if (!adjacentRepeat.isEmpty()) {
            checkVO.getErrorList().add(new ErrorVO(adjacentRepeat, ErrorResultEnum.ADJACENT_REPEAT));
        }
        if (!asLoop.isEmpty()) {
            checkVO.getErrorList().add(new ErrorVO(asLoop, ErrorResultEnum.AS_LOOP));
        }

    }

    /**
     * 3.邻居绕行
     *
     * @param checkVO
     */
    private void ruleThree(CheckVO checkVO, String[] asSplit) {
        List<AsNeighborDTO> list = asNeighborMapper.selectBatchByKeys(Arrays.stream(asSplit).collect(Collectors.toSet()));
        Map<String, Set<String>> neighborMap = list.stream().collect(Collectors.toMap(AsNeighborDTO::getKeyAs, AsNeighborDTO::getNeighborSet));
        for (int i = 0; i < asSplit.length - 1; i++) {
            Set<String> neighbors = neighborMap.get(asSplit[i]);
            if (Objects.isNull(neighbors)) continue;
            if (!neighbors.contains(asSplit[i + 1])) {
                Set<String> set = new HashSet<>();
                set.add(asSplit[i+1]);
                set.add(asSplit[i]);
                checkVO.getErrorList().add(new ErrorVO(set,ErrorResultEnum.FAKE_AS));
            }
//            for(int j = i+2; j < asSplit.length; j++){
//                if (neighbors.contains(asSplit[j])){
//                    Set<String> set = new HashSet<>();
//                    set.add(asSplit[i]);
//                    set.add(asSplit[j]);
//                    checkVO.getErrorList().add(new ErrorVO(set,ErrorResultEnum.TAMPER_AS));
//                }
//            }
        }
    }

    /**
     * 4.国内流量外泄
     *
     * @param checkVO
     * @param asSplit
     */
    private void ruleFour(CheckVO checkVO, String[] asSplit) {
        List<AsNeighborDTO> asList = asNeighborMapper.selectBatchByKeys(Arrays.stream(asSplit).collect(Collectors.toList()));
        Map<String, Integer> collect = asList.stream().collect(Collectors.toMap(AsNeighborDTO::getKeyAs, AsNeighborDTO::getLoc));
        boolean abroad = false;
        int errorStart = -1;
        for (int i = 1; i < asSplit.length; i++) {
            if(!collect.containsKey(asSplit[i - 1]) || !collect.containsKey(asSplit[i - 1])) continue;
            if (collect.get(asSplit[i - 1]) == 1 && collect.get(asSplit[i]) == 0) {
                abroad = true;
                errorStart = i;
            }
            if (abroad && collect.get(asSplit[i - 1]) == 0 && collect.get(asSplit[i]) == 1) {
                Set<String> set = new HashSet<>();
                set.add(asSplit[errorStart]);
                set.add(asSplit[i]);
                checkVO.getErrorList().add(new ErrorVO(set, ErrorResultEnum.NATION_LEAK));
            }
        }
//        for (int i = 1; i < locList.length; i++) {
//            if ((int) locList[i - 1] == 1 && (int) locList[i] == 0) {
//                abroad = true;
//                errorStart = i;
//            }
//            if (abroad && (int) locList[i - 1] == 0 && (int) locList[i] == 1) {
//                Set<String> set = new HashSet<>();
//                set.add(asSplit[errorStart]);
//                set.add(asSplit[i]);
//                checkVO.getErrorList().add(new ErrorVO(set, ErrorResultEnum.NATION_LEAK));
//            }
//        }
    }


    private Set<Integer> cheakAsRepeat(String[] array) {
        Set<Integer> res = new HashSet<>();
        Map<String, Integer> map = new HashMap<>(array.length);
        for (int i = 0; i < array.length; i++) {
            if (map.containsKey(array[i])) {
                res.add(map.get(array[i]));
                res.add(i);
            } else {
                map.put(array[i], i);
            }
        }
        return res;
    }
}
