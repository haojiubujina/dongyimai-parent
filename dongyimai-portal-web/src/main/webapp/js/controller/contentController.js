//广告控制层（运营商后台）
app.controller("contentController",function($scope,contentService){

    //声明一个广告集合
    $scope.contentList=[];

    //根据广告的分类Id查询列表
    $scope.findByCategoryId=function(categoryId){
        contentService.findByCategoryId(categoryId).success(function(response){

                $scope.contentList[categoryId]=response;

            }
        );
    }

    //搜索跳转
    $scope.search=function(){
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});