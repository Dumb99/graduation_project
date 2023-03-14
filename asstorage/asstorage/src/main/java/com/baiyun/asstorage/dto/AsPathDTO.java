package com.baiyun.asstorage.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@TableName("basic_as")
public class AsPathDTO {
    private Long id;
    private String recordType;
    private String time;
    private String project;
    private String collector;
    private String router;
    private String routerIp;
    private String peerAsn;
    private String peerAddress;
    private String prefix;
    private String nextHop;
    private String asPath;
    private String communities;
    private Integer status;

    public AsPathDTO(int status){
        this.status = status;
    }
}
