$(function(){
    $("#uploadForm").submit(upload);
});

function upload() {
    $.ajax({
        url: "http://upload-z2.qiniup.com",
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function(data) {
            if(data && data.code == 0) {
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function(data) {
                        data = $.parseJSON(data);
                        if(data.code == 0) {
                            lightyear.notify('修改成功，页面即将自动跳转~', 'success', 3000);
                            window.setTimeout(function () {
                                window.location.href=CONTEXT_PATH + "/index";
                            }, 3000);

                        } else {
                            lightyear.notify('头像修改失败', 'success', 3000);
                        }
                    }
                );
            } else {
                lightyear.notify('头像修改失败', 'success', 3000);
            }
        }
    });
    return false;
}