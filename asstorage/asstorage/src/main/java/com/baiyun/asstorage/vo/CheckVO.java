package com.baiyun.asstorage.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@NoArgsConstructor
@Document("checkHistory")
public class CheckVO {
    @Id
    private String id;

    private String email;

    private boolean success = true;

    private Set<ErrorVO> errorList;

    private String filename;

    private String time;

    private List<String> asPath;

    public CheckVO(String resultMsg) {
        this.getErrorList().add(new ErrorVO(resultMsg));
    }

    public CheckVO(ErrorVO resultEnum) {
        this.getErrorList().add(resultEnum);
    }

    public List<String> getAsPath(){
        if (Objects.isNull(asPath)){
            asPath = new ArrayList<>();
        }
        return asPath;
    }


    public Set<ErrorVO> getErrorList() {
        if (Objects.isNull(errorList)){
            errorList = new HashSet<>();
        }
        return errorList;
    }
}
