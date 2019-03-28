app.service('loginService',function ($http) { //登录访问层
    this.loginName=function () {
       return $http.get('../login/name.do');//读取登录人的名字
    }
});