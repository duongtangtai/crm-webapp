$(document).ready(function() {
    //------------------CHANGE-PASSWORD-BUTTON-EVENT------------------
    $("#change-password-form").submit(function(event){ 
        event.preventDefault()
        if (confirm("Xác nhận thay đổi mật khẩu?")) {
            var $form = $(this), 
            oldPW = $form.find("input[name='oldPW']").val(),
            newPW = $form.find("input[name='newPW']").val(),
            repeatNewPW = $form.find("input[name='repeatNewPW']").val()
            processChangePassword($form, oldPW, newPW, repeatNewPW)
        }
    })
    //-----------------------------------------FUNCTIONS---------------------------------------
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
    function processChangePassword($form, oldPW, newPW, repeatNewPW) {
        changePassword(oldPW, newPW, repeatNewPW).done(function(result) { 
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processChangePassword($form, oldPW, newPW, repeatNewPW)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else {
                showChangePWResult(result, $form)
            }
        })
    }
    function changePassword(oldPW, newPW, repeatNewPW) {
        return $.ajax({
            method:"POST",
            url:"http://localhost:8080/MyCRM/api/auth/change-password",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data: {
                oldPW : oldPW,
                newPW : newPW,
                repeatNewPW : repeatNewPW
            }
        })
    }
    function showChangePWResult(result, $form) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: result.message,
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $form.find("input[name='oldPW']").val(""),
            $form.find("input[name='newPW']").val(""),
            $form.find("input[name='repeatNewPW']").val("")
        } else {
            $.toast({
                heading: 'Failed',
                text: result.message,
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})