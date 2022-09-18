$(document).ready(function() {
    //welcome user
    var fullName = localStorage.getItem("fullName")
    if (localStorage.getItem("welcome")) {
        $.toast({
            heading: 'Xin chào ' + fullName,
            showHideTransition: 'slide',
            position : "top-center",
            bgColor: '#00ccff',
            textColor: 'white',
            loader: false,
            hideAfter: 3000
        })
        localStorage.removeItem("welcome")
    }
    //get user detail
    processUserProfile()
    //--------------------------------------UPDATE BUTTON EVENT---------------------------------
    $("#update-form").submit(function(event){
        event.preventDefault()
        if (confirm("Xác nhận cập nhật thông tin?")) {
            var userFullName = $("#update-form").find("input[name='fullName']").val(),
            userEmail = $("#update-form").find("input[name='email']").val(),
            userPhoneNum = $("#update-form").find("input[name='phone-num']").val()
            processUpdateProfile(userFullName, userEmail, userPhoneNum)
        }
    })
    //-------------------------------------UPDATA AVATAR EVENT------------------------------------
    $("#avatar-form").submit(function(event) { //add avatar
        event.preventDefault()
        var form = new FormData($("#avatar-form")[0]);
        if (confirm("Xác nhận cập nhật ảnh đại diện?")) {
            processUpdateAvatar(form)
        }
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
    function processUserProfile() {
        getUserProfile().done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUserProfile()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else {
                appendUserProfile(result)
            }
        })
    }
    function getUserProfile() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/profile",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendUserProfile(result) {
        result = result.content;
        $("#fullName").html(result.fullName)
        $("#email").html(result.email)
        if (result.avatar != null) {
            $("#avatar").attr("src",result.avatar)
        }
        //form
        $("#update-form").find("input[name='fullName']").val(result.fullName)
        $("#update-form").find("input[name='email']").val(result.email)
        $("#update-form").find("input[name='phone-num']").val(result.phoneNum)
    }
    function processUpdateProfile(userFullName, userEmail, userPhoneNum) {
        updateProfile(userFullName, userEmail, userPhoneNum).done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateProfile(userFullName, userEmail, userPhoneNum)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else {
                showUpdateProfileResult(result)
            }
        })
    }
    function updateProfile(userFullName, userEmail, userPhoneNum) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/profile",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            },
            data : JSON.stringify({
                fullName : userFullName,
                email : userEmail,
                phoneNum : userPhoneNum
            }),
            contentType: "application/json"
        })
    }
    function showUpdateProfileResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Cập nhật thông tin thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
        } else {
            $.toast({
                heading: 'Failed',
                text: 'cập nhật thông tin thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
    function processUpdateAvatar(form) {
        updateAvatar(form).done(function(result, textStatus, jqXHR) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateAvatar(form)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else {
                showUpdateAvatarResult(result, jqXHR)
            }
        })
    }
    function updateAvatar(form) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/file",
            data : form,
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            contentType: false, 
            processData: false
        })
    }
    function showUpdateAvatarResult(result, jqXHR) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Cập nhật ảnh đại diện thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            localStorage.setItem("avatar", jqXHR.getResponseHeader("avatar")) //save avatar
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Cập nhật ảnh đại diện thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})