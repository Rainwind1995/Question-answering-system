$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	// 获取输入框里标题和内容
	let title = $("#recipient-name").val();
	console.log(title);
	let content = $("#message-text").val();
	// 异步请求
	$.post(
		// 请求路径
		CONTEXT_PATH + "/discuss/add",
		// 请求内容
		{"title":title, "content": content},
		// 回调
		function (data){
			// 将数据转为json格式
			data = $.parseJSON(data);
			// 在提示框中显示返回信息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2秒后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 成功之后刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	)
}