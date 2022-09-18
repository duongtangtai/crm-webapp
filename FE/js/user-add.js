$(document).ready(function() {
    //---------------------------------GET ROLE OPTIONS----------------------------------
    processGetRoleOptions()
    //---------------------------------ADD BUTTON EVENT----------------------------------
    $("#add-form").submit(function(event){
        event.preventDefault()
        if (confirm("Xác nhận thêm thành viên này?")) {
            var $form = $(this)
            userFullName = $form.find("input[name='fullName']").val(),
            userEmail = $form.find("input[name='email']").val(),
            userPassword = $form.find("input[name='password']").val(),
            userPhoneNum = $form.find("input[name='phone-num']").val(),
            userRole = $form.find("select[name='role']").val(),
            processAddUser($form, userFullName, userEmail, userPassword, userPhoneNum, userRole)
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
    function processGetRoleOptions() {
        getAllRoles().done(function(result) {
            if (result.statusCode == 401) { 
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetRoleOptions()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else {
                appendRoleOptions(result)
            }
        })
    }
    function getAllRoles() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/role",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendRoleOptions(result) {
        result = result.content;
        $("#role-options select").empty()
        $.each(result,function(index, value) {
            $("#role-options select").append(`<option>${value.name}</option>`)
        })
    }
    function processAddUser($form, userFullName, userEmail, userPassword, userPhoneNum, userRole) {
        addUser(userFullName, userEmail, userPassword, userPhoneNum, userRole).done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAddUser($form, userFullName, userEmail, userPassword, userPhoneNum, userRole)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                $.toast({
                    heading: 'Failed',
                    text: 'Bạn không có quyền thực hiện!',
                    showHideTransition: 'slide',
                    position : "top-right",
                    icon: 'error',
                    hideAfter: 3000
                })
            } else {
                showAddResult(result, $form)
            }
        })
    }
    function addUser(userFullName, userEmail, userPassword, userPhoneNum, userRole) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/user/add",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : JSON.stringify({
                fullName : userFullName, 
                email : userEmail, 
                password : userPassword,
                phoneNum : userPhoneNum,
                role : userRole,
            }),
            contentType: "application/json"
        })
    }
    function showAddResult(result, $form) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Thêm thành viên thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $form.find("input[name='fullName']").val(""),
            $form.find("input[name='email']").val(""),
            $form.find("input[name='password']").val(""),
            $form.find("input[name='phone-num']").val(""),
            $form.find("select[name='role']").val(""),
            $form.find("input[name='avatar']").val("")
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Thêm thành viên thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})