$(document).ready(function(){
    //TRY TO LOGIN IF THE USER HAS TOKENS
    processCheckToken()
    //------------------LOGIN BUTTON EVENT---------------------------
    $("#login-form").submit(function(event){
        event.preventDefault()
        var $form = $(this), //get value from the form
        inputEmail = $form.find("input[name='email']").val(),
        inputPassword = $form.find("input[name='password']").val()
        $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/auth/login",
            data : {
                email : inputEmail,
                password : inputPassword
            },
            contentType : "application/x-www-form-urlencoded"
        }).done(function(data, textStatus, jqXHR) {
            if (data.successful) { //successful -> store info in localStorage
                var access_token = jqXHR.getResponseHeader("access_token"),
                refresh_token = jqXHR.getResponseHeader("refresh_token"),
                avatar = data.content.avatar,
                fullName = data.content.fullName
                localStorage.setItem("access_token", access_token)
                localStorage.setItem("refresh_token", refresh_token)
                localStorage.setItem("fullName", fullName)
                localStorage.setItem("welcome", true)
                avatar == undefined ? "" : localStorage.setItem("avatar", avatar)
                location.replace("index.html")
            } else {
                $.toast({
                    heading: 'Failed',
                    text: 'Tên đăng nhập hoặc mật khẩu sai!',
                    showHideTransition: 'slide',
                    position : "top-right",
                    icon: 'error',
                    hideAfter: 3000
                })
            }
        })
    })
    //-----------------------------------------FUNCTIONS----------------------------------------
    function refreshToken() {
        return $.ajax({ //access_token is invalid, use refresh_token to get another access_token
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/auth/refresh-token",
            headers : { //if refresh_token can go through the filter. That means it's still valid
                "Authorization" : "Bearer " + localStorage.getItem("refresh_token") 
            }
        })
    }
    function saveTokens(jqXHR) {
        var access_token = jqXHR.getResponseHeader("access_token")
        var refresh_token = jqXHR.getResponseHeader("refresh_token")
        localStorage.setItem("access_token", access_token)
        localStorage.setItem("refresh_token", refresh_token)
    }
    function processCheckToken() {
        checkToken().done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processCheckToken()
                    } else { //if refresh_token is expired. The client have to login again
                        //do nothing
                    }
                })
            } else if (result.statusCode == 403) {
                //do nothing
            } else if (result.successful) { //successful
                location.replace("index.html") 
            }
        })
    }
    function checkToken() {
        return $.ajax({ //Get user information
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/auth/check-token",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
})