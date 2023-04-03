$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	// 获取发送会话目标用户的名字
	let toName = $("#recipient-name").val();
	// 获取发送内容
	let content = $("#message-text").val();

	$.post(
		// 请求路径
		CONTEXT_PATH + "/letter/send",
		// 请求内容
		{"toName": toName, "content": content},
		// 回调
		function (data){
			// 将数据转为json 格式
			data = $.parseJSON(data);
			if(data.code == 0){
				$("#hintBody").text("发送成功");
			}else{
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);

		}
	)

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}