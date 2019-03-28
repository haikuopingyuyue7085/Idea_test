app.service('contentService',function ($http){
    //根据广告类型ID查询列表
    this.findByCategoryId=function (categoryId) {
        //要引的页面index.html在src根目录下不用../
        return $http.get('content/findByCategoryId.do?categoryId='+categoryId);
    }
});