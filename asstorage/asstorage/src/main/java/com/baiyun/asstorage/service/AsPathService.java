package com.baiyun.asstorage.service;

import java.io.IOException;

public interface AsPathService {
    void insertAsPathDto(String filePath) throws IOException;

    String statusCount();
}
