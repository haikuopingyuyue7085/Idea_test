 //控制层 
app.controller('userController' ,function($scope,userService){	


    //用户注册
  $scope.reg=function () {
      //判断两次密码是否一致
      if($scope.entity.password!=$scope.password){
          alert("两次密码输入不一致,请重新输入!");
          $scope.entity.password="";
          $scope.password="";
          return ;
      }

      userService.add($scope.entity,$scope.smsCode).success(
          function (response) {
              alert(response.message)
          }
      );
  }

    //发送短信验证码
    $scope.sendCode=function () {

      //判断是否填写了手机号
        if($scope.entity.phone==""){
            alert("手机号不能为空!");
            return ;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);//发送成功
            }
        )
    }
      

});	
