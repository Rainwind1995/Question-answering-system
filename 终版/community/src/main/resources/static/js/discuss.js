$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});



// 点赞
function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus === 1 ? "已赞" : "赞");
            } else {
                alert(data.msg);
            }
        }
    )
}



// 收藏

function collect(btn, entityType, entityId) {
    $.post(
        CONTEXT_PATH + "/collect",
        {"entityType": entityType, "entityId": entityId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $(btn).children("i").text(data.collectCount);
                // $(btn).children("b").text(data.collectStatus === 1  ? "已收藏" : "收藏");
                $("#collectBtn").text(data.collectStatus === 1 ? '已收藏' : '收藏');
            } else {
                alert(data.msg);
            }
        }
    )
}



// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").text(data.type == 1 ? '取消置顶' : '置顶');
            } else {
                alert(data.msg);
            }
        }
    )
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").text(data.status == 1 ? '取消加精' : '加精');
            } else {
                alert(data.msg);
            }
        }
    )
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index"; // 删除完成跳转回主页
            } else {
                alert(data.msg);
            }
        }
    )
}

// 打开询问是否删除的模态框并设置需要删除的大修的ID
function showDeleteModal(obj) {
    $("#delPost").modal({
        backdrop: 'static',
        keyboard: false
    });
}



