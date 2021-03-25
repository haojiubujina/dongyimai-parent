 //控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,
		 		 typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//将所有的JSON字符串数组转化为JSON字符串
				$scope.entity.brandIds= JSON.parse($scope.entity.brandIds);//转换品牌列表
				$scope.entity.specIds=  JSON.parse($scope.entity.specIds);//转换规格列表
				$scope.entity.customAttributeItems= JSON.parse($scope.entity.customAttributeItems);//转换扩展属性
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//查询品牌下拉列表
	$scope.brandList={data:[]};//品牌列表
	//读取品牌列表
	$scope.findBrandList=function(){
		brandService.selectOptionList().success(
			function(response){
				$scope.brandList={data:response};
			}
		);
	}

	//查询规格下拉列表
	$scope.specList={data:[]};
	$scope.findSpecList=function() {
		specificationService.selectOptionList().success(
			function(response) {
				$scope.specList={data:response};
		})

	}

	//定义同时初始化品牌、规格列表数据
	$scope.initSelect=function(){
		$scope.findSpecList();
		$scope.findBrandList();
	}


	//创建一个实体类，声明一个数组，来接受表单选项规格
	$scope.entity={customAttributeItems:[]}
	//点击新增 调用方法给数组添加数据
	$scope.addTableRow=function() {

		//添加一个数组元素
		$scope.entity.customAttributeItems.push({});
	}

	//移除数组元素
	$scope.deleTableRow=function(index) {

		//删除一个数组元素
		$scope.entity.customAttributeItems.splice(index,1);
	}
/*	$scope.config3 = {
		data: [{id:1,text:'bug'},{id:2,text:'duplicate'},{id:3,text:'invalid'},{id:4,text:'wontfix'}]
		// 其他配置略，可以去看看内置配置中的ajax配置
	};*/

});	