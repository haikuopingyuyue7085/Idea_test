package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference(timeout = 60000)
    private CartService cartService;

    //查找cookie
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {

        String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        //获得当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")) {   //不等于默认角色名就是未登陆
            System.out.println(username + "向cookie读取购物车");
            return cartList_cookie; //向cookie取
        } else {      //已登陆

            List<Cart> cartList_redis =cartService.findCartListFromRedis(username);
            //如果登陆后cookie有新数据,则合并购物车
            if(cartList_cookie.size()>=0){
                List<Cart> cartList=cartService.mergeCartList(cartList_cookie,cartList_redis);
                cartService.saveCartListToRedis(username,cartList);//更新redis
                util.CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行了合并购物车和清空cookie");
                return  cartList;
            }

            return cartList_redis;  //向redis取
        }
    }

    //@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")//可省略,缺省allowCredentials="true"
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
       response.setHeader("Access-Control-Allow-Credentials", "true");


        try {
            //1,从cookie中拿出购物车(判断有没有cookie)
            List<Cart> cartList = findCartList();
            //2,调用购物车方法
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            //获得当前用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (name.equals("anonymousUser")) {   //不等于默认角色名就是未登陆
                //3,存入购物车
                if(cartList.size()<=0){
                    cartList=null;
                }
                String cartListStr = JSON.toJSONString(cartList);
                util.CookieUtil.setCookie(request,response,"cartList",cartListStr,3600*24,"UTF-8");
                System.out.println("向cookie存入购物车"); //向cookie存
            }else{
                cartService.saveCartListToRedis(name,cartList);//向redis存
            }
            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加购物车失败");
        }
    }
}
