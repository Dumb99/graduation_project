package com.baiyun.asstorage.controller;

import com.baiyun.asstorage.mapper.AsPathMapper;
import com.baiyun.asstorage.service.AsNeighborService;
import com.baiyun.asstorage.service.AsPathService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;


@RestController
@CrossOrigin
public class AsPathController {
    @Resource
    private AsPathService asPathService;

    @Resource
    private AsNeighborService asNeighborService;

    @Resource
    private AsPathMapper asPathMapper;

    @GetMapping("/insertBgpStream/{data}")
    public@ResponseBody
    void insertBgp(@PathVariable String data) throws IOException {
        String prefix = "/usr/logs/basic_as/as_collect.";
        asPathService.insertAsPathDto(prefix + data);
    }

    @GetMapping("/hello")
    public@ResponseBody
    String hello()  {
        return "hello world!\n";
    }

    @GetMapping("/handler")
    public @ResponseBody
    void handler(){
        asNeighborService.buildAsNeigh();
    }

    @GetMapping("/setFlag/{flag}")
    public@ResponseBody
    String setFlag(@PathVariable Boolean flag){
        return asNeighborService.setFalg(flag);
    }

    @GetMapping("/getHandlerCount")
    public@ResponseBody
    String getHandlerCount(){
        return "Handler Count: "+asNeighborService.getHandleCount()+"\n";
    }

    @GetMapping("/resetStatus")
    public@ResponseBody
    String resetStatus(){
        asPathMapper.resetStatus();
        return "finish";
    }

    @GetMapping("/statusCount")
    public @ResponseBody
    String statusCount(){
        return asPathService.statusCount();
    }

    @GetMapping("/updateLoc")
    public @ResponseBody
    String updateLoc(){
        return asNeighborService.updateLoc();
    }
}
