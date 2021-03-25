 //用户表控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	$controller('baseController',{$scope:$scope});//继承

	//声明一个用户的实体对象
	$scope.entity={}

	//创建一个注册的方法
	$scope.reg=function(){
		//判断用户名是否为空
		if($scope.entity.username==""||$scope.entity.username==null){
			alert("请输入要注册的用户名");
			return ;
		}
		//判断用户密码是否为空
		if($scope.entity.password==""||$scope.entity.password==null){
			alert("请输入要注册的用户密码");
			return ;
		}
		//判断确认密码是否为空
		if($scope.password==""||$scope.password==null){
			alert("请输入确认密码");
			return ;
		}
		//比对确认密码和密码是否一致
		if($scope.entity.password!=$scope.password){
			alert("两次数的密码不一致");
			return;
		}

		//请输入手机号
		if($scope.entity.phone==""||$scope.entity.phone==null){
			alert("请输入手机号");
			return;
		}

		//请输入短信验证码
		if($scope.code==""||$scope.code==null){
			alert("请输入短信验证码");
			return;
		}

		userService.add($scope.entity,$scope.code).success(function (response) {
			if(response.success){

				location.href="login.html";
			}else{
				alert(response.message);
			}
		});
	}

	//发送验证码
	$scope.sendCode=function(){
		if($scope.entity.phone==null){
			alert("请输入手机号！");
			return ;
		}
		userService.sendCode($scope.entity.phone).success(function(response){
				alert(response.message);
			}
		);
	}

    
});	