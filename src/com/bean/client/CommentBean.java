package bean.client;

import common.db.Pojo;

/**
 * 评论bean
 */
public class CommentBean extends Pojo {
	
	private static final long serialVersionUID = 1L;
	
	/** 序号ID */
	private int id;
	/** 评论ID */
	private String commentId;
	/** 商品ID */
	private String goodsId;
	/** 用户ID */
	private String uid;
	/** 评论内容 */
	private String content;
	/** 创建时间 */
	private long createTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCommentId() {
		return commentId;
	}
	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}
	public String getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
}