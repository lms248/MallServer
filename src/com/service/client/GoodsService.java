package service.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import bean.client.GoodsBean;
import bean.client.ShopBean;
import common.utils.Def;
import common.utils.IdGen;
import common.utils.JsonUtils;
import dao.client.GoodsDao;
import dao.client.ShopDao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 商品
 */
@Controller
@RequestMapping("/goods")
public class GoodsService {
	
	/** 添加商店 */
	@RequestMapping(value ="add",method=RequestMethod.POST)
	@ResponseBody
	public void add(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		System.out.println("-----------");
		
		String name = request.getParameter("name");
		String shopId = request.getParameter("shopId");
		String curPrice = request.getParameter("curPrice");
		String prePrice = request.getParameter("prePrice");
		String marks = request.getParameter("marks");
		String title = request.getParameter("title");
		String details = request.getParameter("details");
		String logo = request.getParameter("logo");
		String imageList = request.getParameter("imageList");
		String thumbList = request.getParameter("thumbList");
		String[] logos = logo.split(";");
		
		System.out.println("shopId===="+shopId);
		if (ShopDao.loadByShopId(Long.parseLong(shopId)) == null) {
			JSONObject obj = new JSONObject();
			obj.put("code", Def.CODE_FAIL);
			obj.put("msg", "该商店名不存在");
			out.print(obj);
			
			out.flush();
			out.close();
			return;
		}
		
		long goodsId = IdGen.get().nextId();
		
		GoodsBean goods = new GoodsBean();
		goods.setGoodsId(goodsId);
		goods.setShopId(Long.parseLong(shopId));
		goods.setCurPrice(Double.parseDouble(curPrice));
		goods.setPrePrice(Double.parseDouble(prePrice));
		goods.setMarks(marks);
		goods.setName(name);
		goods.setTitle(title);
		goods.setDetails(details);
		goods.setLogo(logos[0]);
		goods.setLogoThumb(logos[1]);
		goods.setImageList(JSON.toJSONString(imageList.split(",")));
		goods.setThumbList(JSON.toJSONString(thumbList.split(",")));
		goods.setCreateTime(System.currentTimeMillis());
		
		GoodsDao.save(goods);
		
		JSONObject obj = new JSONObject();
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "添加商品成功");
		obj.put("data", JsonUtils.jsonFromObject(GoodsDao.loadByGoodsId(goodsId)));
		out.print(obj);
		
		out.flush();
		out.close();
	}
	
	/** 商品详情 */
	@RequestMapping(value ="info",method=RequestMethod.GET)
	@ResponseBody
	public void info(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		long goodsId = Long.parseLong(request.getParameter("goodsId")); 
		
		GoodsBean goods = GoodsDao.loadByGoodsId(goodsId);
		
		JSONObject obj = new JSONObject();
		JSONObject obj_data = JSONObject.fromObject(goods);
		ShopBean shop = ShopDao.loadByShopId(goods.getShopId());
		if (shop != null) {
			obj_data.put("shopName", shop.getName());
			obj_data.put("shopLogo", shop.getImage());
			obj_data.put("shopThumb", shop.getThumbnail());
			obj_data.put("contactPhone", shop.getContactPhone());
		}
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "商品详情");
		obj.put("data", JsonUtils.jsonFromObject(obj_data));
		out.print(obj);
		
		out.flush();
		out.close();
	}
	
	/** 商品列表 */
	@RequestMapping(value ="infoList",method=RequestMethod.GET)
	@ResponseBody
	public void infoList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		int index = Integer.parseInt(request.getParameter("index"));//索引开始
		int size = Integer.parseInt(request.getParameter("size"));//条数
		
		List<GoodsBean> goodsList = GoodsDao.loadAllGoods(index, size);
		
		JSONObject obj = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONArray arr = new JSONArray();
		for (int i = 0; i < goodsList.size(); i++) {
			ShopBean shop = ShopDao.loadByShopId(goodsList.get(i).getShopId());
			if (shop == null) {
				continue;
			}
			obj2 = JSONObject.fromObject(JsonUtils.jsonFromObject(goodsList.get(i)));
			//转化成字符串类型
			obj2.put("shopId", ""+goodsList.get(i).getShopId());
			obj2.put("goodsId", ""+goodsList.get(i).getGoodsId());
			obj2.put("shopName", shop.getName());
			obj2.put("shopLogo", shop.getImage());
			obj2.put("shopThumb", shop.getThumbnail());
			obj2.put("contactPhone", shop.getContactPhone());
			arr.add(obj2);
		}
		
		obj.put("code", Def.CODE_SUCCESS);
		obj.put("msg", "商品列表");
		obj.put("data", arr);
		out.print(obj);
		
		System.out.println(obj);

		
		out.flush();
		out.close();
	}
}
