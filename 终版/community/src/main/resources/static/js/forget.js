$(function () {
    $("#getKaptcha").click(getKaptchaCode);
})

function getKaptchaCode() {
    let email = $("#your-email").val();
    if(emailCheck()){
        //执行发送验证码方法
        sendMessage();
        $.post(
            CONTEXT_PATH + "/user/sendResetPwdKatcha",
            {
                "email": email
            },
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    alert("验证码已发送，有效时间2分钟");
                } else {
                    alert("发送失败，请重试!");
                }
            }
        )
    } else{
        alert("请输入正确的邮箱格式!");
    }


}


// 校验邮箱格式
function emailCheck () {
    let email = $("#your-email").val();
    let emailPat=/^(.+)@(.+)$/;
    let matchArray=email.match(emailPat);
    if (matchArray == null) {
        return false;
    }
    return true;
}

let InterValObj; //timer变量，控制时间
let count = 60; //间隔函数，1秒执行
let curCount;//当前剩余秒数

//发送验证码
function sendMessage() {

    curCount = count;
    // //添加禁用按钮类
    $("#getKaptcha").addClass("disabled");
    $("#getKaptcha").text(curCount + "秒后可重新发送");
    // 防止 setInterval 多次注册 导致计时器计时不正确
    window.clearInterval(InterValObj);
    // 启动计时器，1秒执行一次请求后台发送验证码
    InterValObj = window.setInterval(SetRemainTime, 1000);

}


//timer处理函数
function SetRemainTime() {
    if (curCount == 0) {
        window.clearInterval(InterValObj);//停止计时器
        $("#getKaptcha").removeClass("disabled");//移除禁用按钮类
        $("#getKaptcha").text("重新发送验证码");

        return ;
    } else {
        curCount--;
        $("#getKaptcha").text(curCount + "秒后可重新发送");
    }

}









