var app=angular.module('pinyougou',[]);

/*我们测试后发现高亮显示的html代码原样输出，这是angularJS为了防止html攻击采取的安全机制。
我们如何在页面上显示html的结果呢？我们会用到$sce服务的trustAsHtml方法来实现转换。*/

app.filter('trustHtml',['$sce',function($sce){
    return function(data){  //转换前的数据
        return $sce.trustAsHtml(data);//转换后
    }
}]);
