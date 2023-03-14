package com.baiyun.asstorage.service;

import com.baiyun.asstorage.vo.CheckVO;

import java.util.List;
import java.util.Map;

public interface UserService {
    boolean send(String phone, String code);

    Map<String, Object> login(String phone, String code);

    List<CheckVO> findAll(String email);
}
