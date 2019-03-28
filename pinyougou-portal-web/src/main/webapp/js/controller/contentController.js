app.controller('contentController',function ($scope,$location,contentService) {

    //根据广告类型ID查询列表
    //所有广告图片用同一个变量接收但是太乱,定义数组带分类下标
    $scope.contentList=[];
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
              $scope.contentList[categoryId]=response;
            }
        );
    }
    //首页搜索传关键字实现网页跳转,注入$location服务存入关键字
    $scope.search=function () {
        //angular?前得加#
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});