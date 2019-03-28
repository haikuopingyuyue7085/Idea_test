app.service('uploadService',function ($http) {
    
    this.uploadFile=function () {
        var formData= new FormData();//上传文件的二进制载体
        formData.append("file",file.files[0]);//上传文件文本框下标
        return $http({
           method:"post",
           url:"../upload.do",
           data:formData,   //上传的数据变量
            headers:{'Content-Type':undefined},//上传文件的内容类型,不做定义
            transformRequest:angular.identity//对表单数据进行二进制序列化
        });
    }
});