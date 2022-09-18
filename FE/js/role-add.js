$(document).ready(function(){
    //send sth to server to check authorization
    processARole()
    //-----------------------ADD BUTTON EVENT ---------------------------
    $("#add-form").submit(function(event){ 
        event.preventDefault()
        if (confirm("Xác nhận thêm quyền này?")) {
            var $form = $(this), 
            roleName = $form.find("input[name='roleName']").val(),
            roleDescript = $form.find("input[name='roleDescript']").val()
            processAddRole($form, roleName, roleDescript)
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
    function processARole() {
        getARole().done(function(result){ // after ajax() we'll get a result, then we pass it to another function
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processARole()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else {
                //do nothing
            }
        })
    }
    function getARole() {
        return $.ajax({
            method : "GET", 
            url : "http://localhost:8080/MyCRM/api/role/1", 
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function processAddRole($form, roleName, roleDescript) {
        addRole(roleName, roleDescript).done(function(result) { 
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAddRole($form, roleName, roleDescript)
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
    function addRole(roleName, roleDescript) {
        return $.ajax({
            method:"POST",
            url:"http://localhost:8080/MyCRM/api/role/add",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data:JSON.stringify({
                name : roleName,
                description : roleDescript
            }),
            contentType: "application/json"
        })
    }
    function showAddResult(result, $form) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Thêm quyền thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $form.find("input[name='roleName']").val("")
            $form.find("input[name='roleDescript']").val("")
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Thêm quyền thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})