//登录服务层
app.service('loginService',function($http){
    //读取登录人名称
    this.showName=function(){
        return $http.get('../login/showName.do');
    }
});