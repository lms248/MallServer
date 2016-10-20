package service.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import bean.client.FeedbackBean;
import bean.client.GoodsBean;
import bean.client.OrdersBean;
import bean.client.ShopBean;
import bean.client.UserBean;
import common.utils.Def;
import common.utils.IdGen;
import common.utils.JsonUtils;
import common.utils.StringUtils;
import dao.client.FeedbackDao;
import dao.client.GoodsDao;
import dao.client.OrdersDao;
import dao.client.ShopDao;
import dao.client.UserDao;

/**
 * 订单
 */
@Controller
@RequestMapping("/order")
public class OrderService {
	
	/** 创建订单 */
	@RequestMapping(value ="create",method=RequestMethod.POST)
	@ResponseBody
	public void add(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		String token = request.getParameter("token");
		String shopList = request.getParameter("shopList");
		String isFromCart = request.getParameter("isFromCart");
		
		System.out.println("shopList===="+shopList);
		System.out.println("isFromCart===="+isFromCart);
		
		JSONObject obj = new JSONObject();
		
		if (token == null || shopList == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "参数不正确");
			out.print(obj);
			return;
		}
		
		UserBean user = UserDao.loadByToken(token);
		if (user == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "用户不存在");
			out.print(obj);
			return;
		}
		
		//String payId = IdGen.get().nextId()+"";//支付ID
		long createTime = System.currentTimeMillis();
		
		JSONArray shopArr = new JSONArray();
		JSONArray goodsArr = new JSONArray();
		com.alibaba.fastjson.JSONArray orderIds = new com.alibaba.fastjson.JSONArray();
		try {
			shopArr = JSONArray.fromObject(shopList);
		} catch (Exception e) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "参数解析出错");
			out.print(obj);
			return;
		}
		
		double totalPrice = 0;
		for (int i = 0; i < shopArr.size(); i++) {
			JSONObject shopObj = JSONObject.fromObject(shopArr.get(i));
			
			goodsArr = JSONArray.fromObject(shopObj.getString("goodsList"));
			for (int j = 0; j < goodsArr.size(); j++) {
				JSONObject goodsObj = JSONObject.fromObject(goodsArr.get(j));
				totalPrice += GoodsDao.loadByGoodsId(goodsObj.getString("goodsId")).getCurPrice();
				if (isFromCart != null && isFromCart.equals("true")) {
					System.out.println("#goodsId===="+goodsObj.getString("goodsId"));
					System.out.println("#tags===="+goodsObj.getString("tags"));
					int deleteRs = CartService.deleteCart(token, goodsObj.getString("goodsId"), goodsObj.getString("tags"));
					System.out.println("#deleteRs===="+deleteRs);
				}
			}
			
			OrdersBean order = new OrdersBean();
			String orderId = IdGen.get().nextId()+"";//订单ID
			order.setOrderId(orderId);
			//order.setPayId(payId);
			order.setUid(user.getUid());
			order.setShopId(shopObj.getString("shopId"));
			order.setGoodsList(shopObj.getString("goodsList"));
			order.setAddressId(shopObj.getLong("addressId"));
			order.setStatus(Def.ORDER_STATUS_NOPAY);
			order.setCreateTime(createTime);
			
			OrdersDao.save(order);
			orderIds.add(orderId);
		}
		
		/*PayBean pay = new PayBean();
		pay.setPayId(payId);
		pay.setStatus(Def.ORDER_STATUS_NOPAY);
		pay.setCreateTime(createTime);
		PayDao.save(pay);
		
		JSONObject payObj = JSONObject.fromObject(pay);*/
		
		JSONObject orderObject = new JSONObject();
		orderObject.put("totalPrice", totalPrice);
		orderObject.put("orderIds", orderIds);
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "创建订单成功");
		obj.put("data", orderObject);
		out.print(obj);
		
		System.out.println(obj);
		
		out.flush();
		out.close();
	}
	
	/** 订单详情 */
	@RequestMapping(value ="info",method=RequestMethod.GET)
	@ResponseBody
	public void info(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		String orderId = request.getParameter("orderId");
		
		JSONObject obj = new JSONObject();
		JSONObject orderObj = new JSONObject();
		
		OrdersBean order = OrdersDao.loadByOrderId(orderId);
		orderObj = JSONObject.fromObject(JsonUtils.jsonFromObject(order));
		ShopBean shop = ShopDao.loadByShopId(order.getShopId());
		if (shop == null) {
			return;
		}
		JSONArray goodsList = JSONArray.fromObject(order.getGoodsList());
		JSONArray goodsArr = new JSONArray();
		JSONObject goodsObj = new JSONObject();
		double totalPrice = 0;
		for (int i = 0; i < goodsList.size(); i++) {
			goodsObj = JSONObject.fromObject(goodsList.get(i));
			GoodsBean goods = GoodsDao.loadByGoodsId(goodsObj.getString("goodsId"));
			if (goods == null) {
				continue;
			}
			goodsObj.put("goodsName", goods.getName());
			goodsObj.put("goodsLogo", goods.getLogo());
			goodsObj.put("goodsLogoThumb", goods.getLogoThumb());
			goodsObj.put("prePrice", goods.getPrePrice());
			goodsObj.put("curPrice", goods.getCurPrice());
			goodsArr.add(goodsObj);
			totalPrice += goods.getCurPrice() * goodsObj.getInt("amount");
		}
		orderObj.put("shopName", shop.getName());
		orderObj.put("shopLogo", shop.getLogo());
		orderObj.put("shopLogoThumb", shop.getLogoThumb());
		orderObj.put("goodsList", goodsArr);
		orderObj.put("totalPrice", totalPrice);
		
		UserBean user = UserDao.loadByUid(order.getUid());
		JSONArray addressArr = new JSONArray();
		if (!StringUtils.isBlank(user.getAddress())) {
			addressArr = JSONArray.fromObject(user.getAddress());
		}
		JSONObject addressObj = new JSONObject();
		for (int i = 0; i < addressArr.size(); i++) {
			addressObj = JSONObject.fromObject(addressArr.get(i));
			if (order.getAddressId() == addressObj.getLong("addressId")) {
				break;
			}
		}
		orderObj.put("address", addressObj);
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "订单详情");
		obj.put("data", orderObj);
		out.print(obj);
		
		System.out.println(obj);
		
		out.flush();
		out.close();
	}
	
	/** 订单列表 */
	@RequestMapping(value ="infoList",method=RequestMethod.GET)
	@ResponseBody
	public void infoList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		String token = request.getParameter("token");
		String status = request.getParameter("status");
		
		JSONObject obj = new JSONObject();
		
		if (token==null || status == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "参数不正确");
			out.print(obj);
			return;
		}
		
		UserBean user = UserDao.loadByToken(token);
		if (user == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "用户不存在");
			out.print(obj);
			return;
		}
		
		List<OrdersBean> orderList = OrdersDao.loadByUidAndStatus(user.getUid(), Integer.parseInt(status));
		JSONObject orderObj = new JSONObject();
		JSONArray orderArr = new JSONArray();
		for (OrdersBean order : orderList) {
			orderObj = JSONObject.fromObject(JsonUtils.jsonFromObject(order));
			ShopBean shop = ShopDao.loadByShopId(order.getShopId());
			if (shop == null) {
				continue;
			}
			JSONArray goodsList = JSONArray.fromObject(order.getGoodsList());
			JSONArray goodsArr = new JSONArray();
			JSONObject goodsObj = new JSONObject();
			for (int i = 0; i < goodsList.size(); i++) {
				goodsObj = JSONObject.fromObject(goodsList.get(i));
				GoodsBean goods = GoodsDao.loadByGoodsId(goodsObj.getString("goodsId"));
				if (goods == null) {
					continue;
				}
				goodsObj.put("goodsName", goods.getName());
				goodsObj.put("goodsLogo", goods.getLogo());
				goodsObj.put("goodsLogoThumb", goods.getLogoThumb());
				goodsObj.put("prePrice", goods.getPrePrice());
				goodsObj.put("curPrice", goods.getCurPrice());
				goodsArr.add(goodsObj);
			}
			orderObj.put("shopName", shop.getName());
			orderObj.put("shopLogo", shop.getLogo());
			orderObj.put("shopLogoThumb", shop.getLogoThumb());
			orderObj.put("goodsList", goodsArr);
			JSONArray addressArr = new JSONArray();
			if (!StringUtils.isBlank(user.getAddress())) {
				addressArr = JSONArray.fromObject(user.getAddress());
			}
			
			JSONObject addressObj = new JSONObject();
			for (int i = 0; i < addressArr.size(); i++) {
				addressObj = JSONObject.fromObject(addressArr.get(i));
				if (order.getAddressId() == addressObj.getLong("addressId")) {
					break;
				}
			}
			orderObj.put("address", addressObj);
			orderArr.add(orderObj);
		}
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "订单列表");
		obj.put("data", orderArr);
		out.print(obj);
		
		System.out.println(obj);
		
		out.flush();
		out.close();
	}
	
	/** 订单列表 */
	@RequestMapping(value ="infoList2",method=RequestMethod.GET)
	@ResponseBody
	public void infoList2(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		int index = Integer.parseInt(request.getParameter("index"));//索引开始
		int size = Integer.parseInt(request.getParameter("size"));//条数
		String status = request.getParameter("status");//订单状态
		
		List<OrdersBean> orderList = new ArrayList<OrdersBean>();
		
		if (StringUtils.isBlank(status)) {
			orderList = OrdersDao.loadAllOrder(index, size);
		} else {
			switch (Integer.parseInt(status)) {
			case 0:
				
				break;
			case 1:
				
				break;
			case 2:
				
				break;
			case 3:
				
				break;
			case 4:
				
				break;
			case 5:
				
				break;
			default:
				break;
			}
		}
		
		JSONObject obj = new JSONObject();
		JSONObject orderObj = new JSONObject();
		JSONArray orderArr = new JSONArray();
		for (OrdersBean order : orderList) {
			orderObj = JSONObject.fromObject(JsonUtils.jsonFromObject(order));
			ShopBean shop = ShopDao.loadByShopId(order.getShopId());
			if (shop == null) {
				continue;
			}
			orderObj.put("shopName", shop.getName());
			orderObj.put("shopLogo", shop.getLogo());
			orderObj.put("shopLogoThumb", shop.getLogoThumb());
			orderObj.put("createTime2", ""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(order.getCreateTime())));
			orderArr.add(orderObj);
		}
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "订单列表");
		obj.put("count", OrdersDao.Count());
		obj.put("data", orderArr);
		out.print(obj);
		
		System.out.println(obj);
		
		out.flush();
		out.close();
	}
	
	/** 确认收货 */
	@RequestMapping(value ="receive",method=RequestMethod.POST)
	@ResponseBody
	public void receive(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		String token = request.getParameter("token");
		String orderId = request.getParameter("orderId");
		
		JSONObject obj = new JSONObject();
		
		if (token==null || orderId==null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "参数不正确");
			out.print(obj);
			return;
		}
		
		UserBean user = UserDao.loadByToken(token);
		if (user == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "用户不存在");
			out.print(obj);
			return;
		}
		
		OrdersBean order = OrdersDao.loadByOrderId(orderId);
		if (order == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "该订单不存在");
			out.print(obj);
			return;
		}
		
		order.setStatus(Def.ORDER_STATUS_RECEIVE);
		OrdersDao.update(order);
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "确认收货成功");
		out.print(obj);
		
		System.out.println(obj);
		
		out.flush();
		out.close();
	}
	
	/** 取消订单 */
	@RequestMapping(value ="cancel",method=RequestMethod.POST)
	@ResponseBody
	public void cancel(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		String token = request.getParameter("token");
		String orderId = request.getParameter("orderId");
		
		JSONObject obj = new JSONObject();
		
		if (token==null || orderId==null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "参数不正确");
			out.print(obj);
			return;
		}
		
		UserBean user = UserDao.loadByToken(token);
		if (user == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "用户不存在");
			out.print(obj);
			return;
		}
		
		OrdersBean order = OrdersDao.loadByOrderId(orderId);
		if (order == null) {
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "该订单不存在");
			out.print(obj);
			return;
		}
		
		order.setStatus(Def.ORDER_STATUS_CANCEL);
		OrdersDao.update(order);
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "取消订单成功");
		out.print(obj);
		
		System.out.println(obj);
		
		out.flush();
		out.close();
	}
	
	/** 微信商户平台支付结果通知 */
	@RequestMapping(value ="wxPayResult",method=RequestMethod.POST)
	@ResponseBody
	public void wxPayResult(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		System.out.println("---------wxPayResult-------------");
		
		out.flush();
		out.close();
	}
	
}
