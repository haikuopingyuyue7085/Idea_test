package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {

    //新增商品进购物车,传整个购物车对象,skuId,购买数量
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    //向redis取购物车数据
    public List<Cart>  findCartListFromRedis(String username);
    //向redid存购物车数据
    public void saveCartListToRedis(String username,List<Cart> itemList);
    //合并购物车
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
