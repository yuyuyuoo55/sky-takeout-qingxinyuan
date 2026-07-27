package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
//通用接口
public class CommomController {

    @Autowired
    private AliOssUtil aliOssUtil;

    //文件上传
    @PostMapping("/upload")
    public Result<String> upload(@RequestBody MultipartFile file)
    {
        log.info("文件上传:{}",file);

        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();

            //获取文件名后缀
            String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

            //创建新文件名
            String filename = UUID.randomUUID().toString()+ substring;

            //文件上传
            String filePath = aliOssUtil.upload(file.getBytes(), filename);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
