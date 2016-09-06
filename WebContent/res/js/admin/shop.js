/**
 * 商店js
 */

/**
 * 添加或编辑商店
 */
function shop_edit() {
	var name = $("#name").val();
	var title = $("#title").val();
	var details = $("#details").val();
	var images = $("#show_image").attr("alt");
	var contactPhone = $("#contactPhone").val();
	
	if (name == "") {
		alert("请输入商店名称！！！");
	}
	else if (title == "") {
		alert("请输入标题！！！");
	}
	else if (details == "") {
		alert("请输入描述内容！！！");
	}
	else if (images == "") {
		alert("请上传商店Logo！！！");
	}
	else {
		var tip = "你确认"+$("#shop-edit-submit").text()+"吗？";
		if(confirm(tip)){
			var params = {name:name,title:title,details:details,images:images,contactPhone:contactPhone};
			$.post("/shop/add",params,function(data){
				alert(data.msg);
				if(data.code=="0"){
					getShopDateList(0);
				}
				$("#modalCloseBtn").click();
				$("#shop-edit-submit").html("修改");
			},"json");
		}
	}
}

/**
 * 添加商品
 */
function goods_add() {
	var goods_name = $("#goods_name").val();
	var shopId = $("#shop_name").attr("title");
	var goods_curPrice = $("#goods_curPrice").val();
	var goods_prePrice = $("#goods_prePrice").val();
	var goods_tagKey = $("#goods_tagKey").val();
	var goods_tagValue = $("#goods_tagValue").val();
	var goods_title = $("#goods_title").val();
	var goods_details = $("#goods_details").val();
	var goods_logo = $("#goods_logo").attr("alt");
	
	if (goods_name == "") {
		alert("请输入商品名称！！！");
	}
	else if (shopId == "") {
		alert("请输入商店ID！！！");
	}
	else if(goods_curPrice==""){
		alert("请输入商品原价！！！");
	}
	else if(isNaN(parseFloat($("#goods_curPrice").val()))){
		alert("商品原价类型不正确！！！");
	}
	else if(goods_prePrice==""){
		alert("请输入商品当前售价！！！");
	}
	else if(isNaN(parseFloat($("#goods_prePrice").val()))){
		alert("商品当前售价类型不正确！！！");
	}
	else if (goods_tagKey == "") {
		alert("请输入标签名字！！！");
	}
	else if (goods_tagValue == "") {
		alert("请输入标签值！！！");
	}
	else if (goods_title == "") {
		alert("请输入标题！！！");
	}
	else if (goods_details == "") {
		alert("请输入描述内容！！！");
	}
	else if (goods_logo == "") {
		alert("请上传商品Logo！！！");
	}
	else if (goods_imageList == "") {
		alert("请上传商品图片列表！！！");
	}
	else {
		var tip = "你确认添加吗？";
		if(confirm(tip)){
			goods_tags = goods_tagKey+":"+goods_tagValue+";";
			alert(goods_tags);
			var params = {name:goods_name,shopId:shopId,curPrice:goods_curPrice,prePrice:goods_prePrice,
					tags:goods_tags,title:goods_title,details:goods_details,logo:goods_logo,imageList:goods_imageList,thumbList:goods_thumbList};
			$.post("/goods/add",params,function(data){
				if(data.code=="0"){
					alert("添加商品成功！！！");
					$("#goods_modalCloseBtn").click();
				} else {
					alert(data.msg);
				}
			},"json");
		}
	}
}


/**
 * 查看商店
 * @param shopId
 */
function showShop(shopId) {
	updateShop(shopId);
}

/**
 * 修改商店
 * @param shopId
 */
function updateShop(shopId) {
	$.get("/shop/info",{shopId:shopId},function(data){
		if(data.code=="0"){
			$("#name").val(data.data.name);
			$("#title").val(data.data.title);
			$("#details").val(data.data.details);
			$("#show_image").attr("src",data.data.image);
			$("#contactPhone").val(data.data.contactPhone);
			$("#shop-edit-submit").html("修改");
		} else {
			alert(data.msg);
		}
	},"json");
}

/**
 * 删除商店
 * @param shopId
 */
function deleteShop(shopId) {
	$.get("/shop/delete",{shopId:shopId},function(data){
		alert(data.msg);
		if(data.code=="0"){
			getShopDateList(0);
		}
	},"json");
}


/**
 * 重置编辑
 */
function resetEdit() {
	$("#name").val("");
	$("#title").val("");
	$("#details").val("");
	$("#contact_phone").val("");
}

/**
 * 获取商店数据列表
 */
function getShopDateList(index) {
	var pageSize = $('#pageSize').val();
	var params = {index:index,size:pageSize};
	$.get("/shop/infoList",params,function(data){
		if(data.code=="0"){
			var template = $.templates("#tableTmpl");
			var htmlOutput = template.render(data.data);
			$("#shopTableData").html(htmlOutput);
			$("#allCount").html(data.count);
		} else {
			alert(data);
		}
	},"json");
}

$("#pageSize").change(function(){
	getShopDateList(0)
});

