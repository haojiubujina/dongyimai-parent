app.controller("indexController",function($scope,loginService) {

    //获取当前用户信息
    $scope.showLoginName=function() {

        loginService.showName().success(function(response) {
            $scope.loginName=response.showName;

        })
    }
})