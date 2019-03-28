package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    //新增商品进购物车,传整个购物车对象,skuId,购买数量
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1,根据skuId查询商品sku详细信息
        TbItem item =itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("该商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品已下架");//时间差,后台可能改了
        }
        //2,根据sku详细信息得到商家sellerId(据此分京东自营还是第三方卖家)
        String sellerId = item.getSellerId();
        //3,根据sellerId查询购物车对象
         Cart cart = searchCartBySellerId(cartList, sellerId);
        //4,如果购物车列表中不存在该商家的购物车
        if(cart==null){
            //4.1,那么就创建一个新的购物车对象
             cart = new Cart();
            //4.2将新的购物车对象添加到购物车购物车列表
            cart.setSellerId(sellerId);//商家id
            cart.setSellerName(item.getSeller());//商家名称
            //创建购物车项列表
            List<TbOrderItem> orderItemList = new ArrayList();
            //创建购物车项并添加列表
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart); //将购物车对象添加到购物车列表
        }else{
            //5判断购物车列表中存在该商家的购物车
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem==null){
                //5.1,如果在购物车中不存在该商品,则创建购物车项
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);//添加进购物车
            }else{
                //5.2,如果存在,则再原有数量上+num,并更新总计
                orderItem.setNum(orderItem.getNum()+num);//更新数量
                //更新项总价
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //数量减到0,移除列表的项
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }
                //当车内没有项则移除车
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    //向redis取购物车数据
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("向redis取购物车数据"+username);
       List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
       if(cartList==null){
           cartList=new ArrayList<>();
       }
        return cartList;
    }
    //向redis取购物车数据
    @Override
    public void saveCartListToRedis(String username, List<Cart> itemList) {
        redisTemplate.boundHashOps("cartList").put(username,itemList);
        System.out.println("向redis存购物车数据"+username);

    }

    //合并购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for(Cart cart:cartList2){   //购物车列表
            for (TbOrderItem orderItem:cart.getOrderItemList()){//购物车项列表
                //将List2的每一项添加到1
                cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        System.out.println("合并购物车");
        return cartList1;
    }

    //判断购物车列表中存在该商家的购物车
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){//Long是对象==比较的地址值,转long则比较值
                return orderItem;
            }
        }
        return null;

    }
    //创建购物车项并添加列表
    private  TbOrderItem createOrderItem(TbItem item,Integer num){
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());//商品id就是skuId
        orderItem.setItemId(item.getId());//项Id
        orderItem.setNum(num);//数量
        orderItem.setPicPath(item.getImage());//图片路径
        orderItem.setPrice(item.getPrice());//价格
        orderItem.setSellerId(item.getSellerId());//商家id
        orderItem.setTitle(item.getTitle());
        //big的价格转double,总价再转回big,购物车项总价
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    //在购物车列表查询有无该商家的购物车(根据sellerId)
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){
        for (Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;    //有则创建购物车
            }
        }
        return null;
    }
}
