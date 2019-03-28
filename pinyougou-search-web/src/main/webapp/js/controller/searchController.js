app.controller('searchController',function($scope,$location,searchService){

    //定义过滤条件变量(搜索对象)
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':15,'sort':'','sortField':''};//规格有多个选项,又得封成对象{}

    //搜索条件
    $scope.search=function(){
        //输入第几页快捷查询:需在前端将这里的字符串转Integer,否者500
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function(response){
                //map.put("rows", page.getContent());//获取page集合中的内容放进map
                $scope.resultMap=response;
                buildPagelabel();//调用分页方法
            }
        );
    }
    //分页
    buildPagelabel=function(){
        //有几页就往数组内添加
        $scope.pagelabel=[];//共有多少页

        var firstPage=1;//首页
        var lastPage=$scope.resultMap.totalPages;//尾页

        $scope.firstDot=true;//前面有点...
        $scope.endDot=true;//后面有点...

        //分页栏下标随着点击当前页页码的变化而变化
        if($scope.resultMap.totalPages>5){//总页数小于5则显示前5页即可
            if($scope.searchMap.pageNo<=3){//当前页码小于3则显示前5页,1-5
                lastPage=5;
                $scope.firstDot=false;
            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){//当前页码=总页数-2
                firstPage=$scope.resultMap.totalPages-4;//显示后5页,96-100
                $scope.endDot=false;
            }else{
                //以当前页码为中心,16 * 18 * 20
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else{  //页数只有前5页,前后都不显示...
            $scope.firstDot=false;
            $scope.endDot=false;
        }
        for(var i=firstPage;i<lastPage;i++){
            $scope.pagelabel.push(i);
        }
    }

    //添加搜索项
    $scope.addSearchItem=function (key,value) {

        if(key=='category' ||key=='brand' ||key=='price'){//搜索得是分类或者是品牌
            $scope.searchMap[key]=value;
        }else{  //规格
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//增删的同时,搜索结果相应变化
    }
    //移除搜索项
    $scope.removeSearchItem=function (key) {

        if(key=='category' ||key=='brand' ||key=='price'){
            $scope.searchMap[key]="";
        }else{  //规格
           delete $scope.searchMap.spec[key];//删除对象中的属性Key.
        }
        $scope.search();//增删的同时,搜索结果相应变化
    }
    //点击页码查询
    $scope.queryByPage=function (pageNo) {
        if(pageNo<1 ||pageNo>$scope.resultMap.totalPages){//没有上一页或者下一页,返回空结束不继续查询
            return ;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();//调用查询方法
    }
    //无上一页
    $scope.topPage=function () {
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }
    //无下一页
    $scope.endPage=function () {
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }
    //字段升降序
    $scope.seachSort=function (sortField,sort) {
        $scope.searchMap.sortField=sortField;//前端传参接收
        $scope.searchMap.sort=sort;
        $scope.search();//查询
    }
    //判断关键词是否包含品牌
    $scope.keywordesIsbrand=function () {
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//{"2":"三星"}
                return true;
            }
        }
        return false;
    }
    //首页搜索传关键字实现网页跳转(这为接收参数,同样注入$location服务取出关键词,还得页面初始化)
    $scope.loadkeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();//查询
    }
});