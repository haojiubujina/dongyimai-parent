//定义了一个叫myApp的模块
//引入分页模块
var app=angular.module("dongyimai",[]);

/*$sce服务写成过滤器*/
app.filter('trustHtml',['$sce',function($sce){
    return function(data){
        return $sce.trustAsHtml(data);
    }
}]);