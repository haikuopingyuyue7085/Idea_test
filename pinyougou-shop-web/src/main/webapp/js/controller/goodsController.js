 //控制层 $location一个html页面带所有或者id跳转到另一个页面
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
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
	
	//查询实体 /点修改前回显
	$scope.findOne=function(){
        var id= $location.search()['id'];//获取参数值,注意： ?前要加# ，则是angularJS的地址路由的书写形式 #?id=149187842867969
		if(id==null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction);//商品介绍(富文本编辑器)
				//图片url字符串转回图片
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//商品扩展属性(字符串转,去key显示value)
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //商品的规格
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//规格选项sku
				for (var i=0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		var scopeObject;
		$scope.entity.goodsDesc.introduction = editor.html();//从富文本编辑器提取内容存数据库
		//修改
		if($scope.entity.goods.id!=null){
			scopeObject=goodsService.update($scope.entity);
		}else{
			//新增
            scopeObject=goodsService.add($scope.entity);
		}
        scopeObject.success(
        	function (response) {
				if(response.success){
					alert("保存成功");
					location.href="goods.html";//跳转到首页
				}else{
					alert("保存失败");
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
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
				if(response.success){
                    $scope.image_entity.url=response.message;//设置文件地址
				}else{
					alert(response.message);
				}

        });
    }
    //上传多张图片()
    //上传的图片信息以集合方式存储并显示,复选框集合
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);//上传的每一张图片以一个变量接收
    }
    //删除图片列表
    $scope.remove_image_entity=function ($index) {
        $scope.entity.goodsDesc.itemImages.splice($index,1);
    }

    //下拉列表1
	$scope.selectItemCat1List=function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List=response;
            });
    }
    //下拉列表2,$watch监控entity.goods.category1Id变量的变化
   $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {//新放前,旧放后
	   itemCatService.findByParentId(newValue).success(
	   	function (response) {
			$scope.itemCat2List=response;
        });
   });
    //下拉列表3,$watch监控entity.goods.itemCat2List变量的变化
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List=response;
            });
    });
    //模板Id
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        itemCatService.findOne(newValue).success(	//查询tb_item_cat的typeId存到goods的typeTemplateId
       		function (response) {
			$scope.entity.goods.typeTemplateId=response.typeId;	//抽取字符串中的typeId
            });
    });
	//品牌下拉列表
	$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
		typeTemplateService.findOne(newValue).success(
			function (response) {
				$scope.typeTemplate=response;//拿到表中的多个字段
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);	//截取多个品牌字符串中的品牌字段
				if($location.search()['id']==null){//修改为null是为新增(没if判断会覆盖上面findOne修改方法的值)
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				}
            }
		)
		//规格选项列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList=response;
            });
    });
    //保存选中规格选项(某表某字段的List<map>,判断有头则直接存尾,否者先存头再存尾)
	$scope.updateSpecAttribute=function ($event,name,value) {
		//调用baseController中的判断集合中有没有头(ps如网络格式)
		var object=$scope.searchObjectByKey(
			$scope.entity.goodsDesc.specificationItems,'attributeName',name);
		//有头,则直接添加尾
		if(object!=null){
			if($event.target.checked){
				//将尾值放进集合中
				object.attributeValue.push(value);
			}else{
				//取消勾选,去尾值的索引
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                //如果选项都取消了，将此条记录移除[]
				if(object.attributeValue.length==0){
					//移除整个集合(字段)
					$scope.entity.goodsDesc.specificationItems.splice(
						$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			//无头,新建
			$scope.entity.goodsDesc.specificationItems.push(
				{"attributeName":name,"attributeValue":[value]});
		}
    }

    //创建SKU列表(难点重点:深克隆,就是每次"组合"网络和内存,在集合中新增排列组合,不是固定一个旧的集合)
    $scope.createItemList=function(){
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始
        var items=  $scope.entity.goodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
        }
    }
//添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
                var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆,JSON.stringify将通常为对象或数组值转换为 JSON 字符串
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    $scope.status=["未审核","审核通过","审核未通过","已关闭"];//0,1,2,3
	//商品三级分类信息
	$scope.itemCatList=[];//存name
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
            function (response) {
                for (var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name
                }
            }
		);

	}

    //根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function (specName,optionName) {//规格名称(网络),选项名称(联通3g,电信2g),value
		//拿到表的规格字段
		var items=$scope.entity.goodsDesc.specificationItems;
		//判断该集合中有无头(传key和value)
		var object=$scope.searchObjectByKey(items,'attributeName',specName);//key
		if(object==null){
			return false;//不勾选
		}else{
			//有头,值得索引从0,1开始,有勾选
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
    }
    //修改商品上下架
	//定义上下架
	$scope.marketable=['上架','下架'];
    $scope.updateMarketable=function (isMarketable) {
		goodsService.updateMarketable($scope.selectIds,isMarketable).success(
			function (response) {
				if(response.success){
					$scope.reloadList();//刷新
					$scope.selectIds=[];
				}else{
					alert(response.message);
				}
            }
		);
    }
});
