package bean.client;

import common.db.Pojo;

/**
 * 订单bean
 * */
public class OrdersBean extends Pojo {
	
	private static final long serialVersionUID = 1L;
	
	/** 序号ID */
	private int id;
	/** 订单ID */
	private String orderId;
	/** 订单支付ID */
	private String payId;
	/** 用户ID */
	private String uid;
	/** 店铺ID */
	private String shopId;
	/** 商品订单列表信息 */
	private String goodsList;
	/** 收货地址ID */
	private long addressId; 
	/** 订单状态 */
	private int status;
	/** 下单时间 */
	private long createTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getShopId() {
		return shopId;
	}
	public void setShopId(String shopId) {
		this.shopId = shopId;
	}
	public String getGoodsList() {
		return goodsList;
	}
	public void setGoodsList(String goodsList) {
		this.goodsList = goodsList;
	}
	public long getAddressId() {
		return addressId;
	}
	public void setAddressId(long addressId) {
		this.addressId = addressId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
}
