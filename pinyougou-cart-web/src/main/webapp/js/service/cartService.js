//购物车服务层
app.service('cartService',function($http){
    //购物车列表
    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    }
    //添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }
    
    //总计
    this.sum=function (cartList) {
        var totalValue={totalNum:0,totalMoney:0};//数量.总金额

        for(var i=0;i<cartList.length;i++){  //遍历购物车列表
            var cart=cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){    //遍历购物车项列表
                var orderItem=cart.orderItemList[j];
                totalValue.totalNum+=orderItem.num;          //得到每一个购物车项总数量
                totalValue.totalMoney+=orderItem.totalFee;  //得到每一个购物车项总金额
            }
        }
        return totalValue;
    }
});
