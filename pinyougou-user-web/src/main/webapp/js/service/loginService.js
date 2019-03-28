app.service('loginService',function ($http) {
    //登录用户名
    this.showName=function () {
        return $http.get('../login/showName.do');
    }
});