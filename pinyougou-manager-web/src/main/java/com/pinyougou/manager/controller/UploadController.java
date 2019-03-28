package com.pinyougou.manager.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

//文件上传
@RestController
public class UploadController {
    //错的:http://localhost:9102/admin/$FILE_SERVER_URLgroup1/M00/00/00/wKgZhVxo6jeAepcPAASGCRcqUj0997.jpg
    //对的:http://192.168.25.133/group1/M00/00/00/wKgZhVxoz1yAY23TAAEMHup01Jo704.jpg

    @Value("${FILE_SERVER_URL}")//少写{}图片可以上传,但是不回显,不是完整地址
    private String FILE_SERVER_URL;//文件服务器的地址

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){   //文件对象
        //1,获得文件的扩展名
        String originalFilename = file.getOriginalFilename();
        String extName =originalFilename.substring(originalFilename.lastIndexOf(".")+1);//获取文件名字符串.之后的字符串(后缀名)
        //2,创建一个FastDFS的客户端
        try {
            FastDFSClient fastDFSClient
                    = new FastDFSClient("classpath:config/fdfs_client.conf");
            //3,执行上传处理
            String fileId= fastDFSClient.uploadFile(file.getBytes(),extName);//文件转成字节数组,保存的后缀名
            //4,拼接返回的url和fileId
            String url= FILE_SERVER_URL + fileId;//一个完整的文件地址(分布式文件服务器ip地址+文件Id)
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }
}
