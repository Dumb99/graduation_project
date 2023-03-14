package com.baiyun.asstorage.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@TableName("adj_as")
public class AsNeighborDTO {
    private String keyAs;
    private String nhbAs;
    private Integer loc;
    private String p2p;
    private String p2c;

    public AsNeighborDTO(String keyAs, String nhbAs){
        this.keyAs = keyAs;
        this.nhbAs = nhbAs;
    }

    public AsNeighborDTO(String keyAs, Set<String> adjAs){
        this.keyAs = keyAs;
        this.nhbAs = StringUtils.collectionToDelimitedString(adjAs," ");
    }

    public void resetNhbAs(Set<String> adjAs){
        nhbAs = StringUtils.collectionToDelimitedString(adjAs," ");
    }

    public Set<String> getNeighborSet(){
        return Arrays.stream(nhbAs.split(" ")).collect(Collectors.toSet());
    }

    public boolean inNation(){
        return this.getLoc() == 1;
    }
    public boolean outNation(){
        return this.getLoc() == 0;
    }

}
