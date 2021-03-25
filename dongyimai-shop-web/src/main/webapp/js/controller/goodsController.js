	 //控制层
app.controller('goodsController' ,function($scope,$controller,$location,
										  itemCatService,goodsService,uploadService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){

		var id= $location.search()['id'];//获取参数值
		alert("id:"+id);
		if(id==null) {
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);

				//显示图片列表,将json字符串转化为数组对象
				$scope.entity.goodsDesc.itemImages=
					JSON.parse($scope.entity.goodsDesc.itemImages);

				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems=
					JSON.parse($scope.entity.goodsDesc.customAttributeItems);

				//显示规格
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

				//SKU列表规格列转换
				for( var i=0;i<$scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec =
						JSON.parse( $scope.entity.itemList[i].spec);
				}
			}
		);				
	}

	//根据规格和规格选项，判断该规格选项是否被选中
	$scope.checkAttributeValue=function(specName,optionName){

		var items= $scope.entity.goodsDesc.specificationItems;
		//查询items中是否存在 specName对应的一个数组对象
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){ //不存在 false
			return false;
		}else{//存在

			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	//保存 
	$scope.save=function(){

		//取出富文本编辑器中的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){ //成功
					alert('操作成功');
					//清空缓存中的商品数据
					$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
					//清空富文本编辑器中的数据
					editor.html("");
					location.href="goods.html";//跳转到商品列表页
				}else{ //失败
					alert(response.message);
				}
			}		
		);				
	}

	//保存
	$scope.add=function(){

		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert('保存成功');
					//清空缓存中的商品数据
					$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
					//清空富文本编辑器中的数据
					editor.html("");
				}else{
					alert(response.message);
				}
			}
		);
	}
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//上传图片
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response) {
			if(response.success){//如果上传成功，取出url
				$scope.image_entity.url=response.message;//设置文件地址
			}else{
				alert(response.message);
			}
		}).error(function() {
			alert("上传发生错误");
		});
	};


	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	//列表中移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}


	//读取一级分类
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
			function(response){
				$scope.itemCat1List=response;
			}
		);
	}

	//读取二级分类
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue) {
		//判断一级分类有选择具体分类值，在去获取二级分类
		if(newValue){
			//根据选择的值，查询二级分类
			itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat2List=response;
				}
			);
		}
	})

	//读取三级分类
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue) {
		//判断二级分类有选择具体分类值，在去获取三级分类
		if(newValue){
			//根据选择的值，查询二级分类
			itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat3List=response;
				}
			);
		}
	})

	//三级分类选择后  读取模板ID
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
		//判断三级分类被选中，在去获取更新模板id
		if(newValue){
			itemCatService.findOne(newValue).success(
				function(response){
					$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
				}
			);
		}
	});

	//根据typeId的变化，查询模板，进而取出品牌，规格等
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue) {
		//根据模板的Id---typeId变换取出相关信息
		if(newValue) {
			//根据id 查询模板
			typeTemplateService.findOne(newValue).success(function(response) {
				$scope.typeTemplate=response;//取出查询的模板对象
				//品牌将json字符串转换为数组对象
				$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
				//扩展属性
				if($location.search()['id']==null) {
					$scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);
				}
			})

			//查询规格列表
			typeTemplateService.findSpecList(newValue).success(
				function(response){
					$scope.specList=response;
				});
		}
	})

	//创建一个方法，用来处理选择规格选项和移除规格选项的处理过程代码
	$scope.updateSpecAttribute=function($event,name,value) {
		//查询name对应的对象是否存在
		var object=$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName', name);
		if(object!=null) {//原来数组有这个对象
			//当前这个规格选项是不是被选中，还是移除
			if($event.target.checked) {//选中
				object.attributeValue.push(value);

			}else{//移除
				//找到要移除数组元素的位置
				var index=object.attributeValue.indexOf(value);
				//移除
				object.attributeValue.splice(index,1);

				//判断attributeValue的数组没有一个元素，就将当前这个对象从$scope.entity.goodsDesc.specificationItems移除
				if(object.attributeValue.length==0) {
					//先找到位置
					var indexV = $scope.entity.goodsDesc.specificationItems.indexOf(object);
					//移除
					$scope.entity.goodsDesc.specificationItems.splice(indexV,1);

				}
			}
		}else{//原来没有这个对象
			//初次就添加一个
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});

		}
	}

	//创建sku列表
	$scope.createItemList=function() {
		//初始化一个sku列表
		$scope.entity.itemList=[{spec:{},price:0,num:100,status:'0',isDefault:'0'}];
		//初始
		var items = $scope.entity.goodsDesc.specificationItems;
		//遍历数组找到规格选项，对应的数量
		for(var i=0;i<items.length;i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}

	//添加列值
	addColumn=function(list,columnName,columnValues) {
		var newList=[];//新的集合
		for(var i=0;i<list.length;i++) {

			var oldRow = list[i];

			for(var j=0;j<columnValues.length;j++) {
				//深克隆
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}

		}
		return newList;
	}

	//创建一个数组表示审核状态的可选项
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

	$scope.itemCatList=[];//商品分类列表

	//加载商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
			function(response){
				for(var i=0;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		);
	}

});