package com.dzy.peqa.controller;

import cn.hutool.json.JSONUtil;
import com.dzy.peqa.result.ResultVO;
import com.dzy.peqa.service.GenOptionImage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * phantomjs
 *
 * @author Zephyr.Shane
 * @version 2025/07/02 15:37
 **/
@RestController
@RequestMapping("/gen")
public class PhantomjsController {

    @Resource
    private GenOptionImage genOptionImage;

    @PostMapping("/image")
    public ResultVO genOptionImage(@RequestBody String option) {
        return ResultVO.success(genOptionImage.execute(JSONUtil.toBean(option, OptionReq.class).getOption()));
    }


}
