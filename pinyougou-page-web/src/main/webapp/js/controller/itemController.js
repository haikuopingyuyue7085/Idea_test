app.controller('itemController' ,function($scope){
  
  //商品数量+-
  $scope.num=1;//初始为1

  $scope.changeNum=function(x){
	  $scope.num+=x;//总数
	  if($scope.num<1){
		  $scope.num=1;
	  }
  }
  //存入选中的规格
  $scope.specificationItems={};
  $scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;
		searchSku();//更新sku
	  }

	//判断该规格选项是否被选中(再从对象取出来key==value)
	$scope.isSelectSpec=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;
		}else{
			return false;
			}
	}
	
	$scope.sku={};	//当前选中的SKU
	//加载默认SKU,第一个
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));//更新选中的规格数组:深克隆(两个对象)的字符串再转为对象

		}
	//选择规格更新SKU,可能map1有2个key,map2有3个key所以需要双向循环判断
	matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	//在SKU列表中查询当前用户选择的SKU
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems )){
				$scope.sku=skuList[i];
				return ;
			}
	}
	$scope.sku={id:0,title:'-----',price:0};//如果没有匹配
	}
	//模拟添加购物车
	$scope.addToCar=function(){
		alert("skuId:" +$scope.sku.id);
	}
});