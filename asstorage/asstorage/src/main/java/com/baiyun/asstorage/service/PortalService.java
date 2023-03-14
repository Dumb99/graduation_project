package com.baiyun.asstorage.service;

import com.baiyun.asstorage.vo.CheckVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PortalService {
    CheckVO singleCheck(String asPath);

    CheckVO singleCheck(String asPath, String email);

    List<CheckVO> bactchCheck(MultipartFile file,String email);
}
