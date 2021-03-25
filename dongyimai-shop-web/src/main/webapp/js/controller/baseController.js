
//定义控制器
app.controller("baseController",function($scope){



    //重新加载页面
    $scope.reloadList=function(){

        //分页查询数据
        $scope.search($scope.paginationConf.currentPage , $scope.paginationConf.itemsPerPage);

    }


    //配置一下分页控件
    $scope.paginationConf={

        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [5, 10, 30, 40, 50],

        onChange: function(){

            $scope.reloadList();//重新加载
        }
    }



    //声明一个数组
    $scope.selectIds=[];

    //添加id到数组中 是否要添加
    $scope.updateSelection = function ($event, id) {

        //如果被选中 ，则增加到数组
        if($event.target.checked) {
            $scope.selectIds.push(id);

        }else {
            //找到id在数组中的位置
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);//删除该坐标位置数据
        }
    }


    //初始化一个查询条件对象
    $scope.searchEntity={};

    //将数组对象转化一个只包含某一个属性的字符串
    $scope.jsonToString=function(jsonString,key) {

        //定义一个变量保存取出的数据
        var value="";
        var json=JSON.parse(jsonString);
        //遍历数组对象
        for(var i=0 ; i<json.length ; i++) {

            if(i>0){
                value+=","
            }
            value+=json[i][key];

        }
        return value;
    }


    //从集合中按照key查询对象
    $scope.searchObjectByKey=function(list,key,keyValue){
        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }

})