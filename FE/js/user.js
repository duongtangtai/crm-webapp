$(document).ready(function(){
    //-------------------------------------FILL USER INFO------------------------------------
    processAllUsers()
    //-----------------------------------DELETE BUTTON EVENT----------------------------------------
    $("body").on("click",".btn-delete",function() {
        if (confirm("Xác nhận xóa thành viên này?")) {
            var userId =  $(this).attr("user-id")
            processDeleteUser(userId)
        }
    })
    //-----------------------------------UPDATE BUTTON EVENT----------------------------------------
    $("body").on("click",".btn-update",function() {
        if ($(this).text() == "Sửa") { 
            $(this).html("Cập nhật")
            $form = $(this).closest("tr"), //LET CLIENT EDIT INFORMATION
            $form.children("td.userFullName").attr("contenteditable",""),
            $form.children("td.userEmail").attr("contenteditable",""),
            $form.children("td.userPhoneNum").attr("contenteditable",""),
            $role = $form.children("td.userRole") //EXTRACT THE ROLE OUT
            var oldRole = $role.text()//STORE TEXT IN A VAR TO COMPARE LATER
            $role.empty().html("<select></select>") //CREATE A SELECT TAG INSIDE
            $select = $role.children("select")
            processGetRoleOptions($select, oldRole)
        } else if ($(this).text() == "Cập nhật") {
            //GET NEW VALUES LIKE A FORM
            var $form = $(this).closest("tr"),
            userId = $form.children("td.userId").text(),
            userFullName = $form.children("td.userFullName").text(),
            userEmail = $form.children("td.userEmail").text(),
            userPhoneNum = $form.children("td.userPhoneNum").text(),
            userRole = $form.children("td.userRole").children("select").val();
            if (userEmail.endsWith("@gmail.com") && userEmail != "@gmail.com") { //check whether email valid 
                if (confirm("Xác nhận sửa thành viên này?")) {
                    processUpdateUser(userId, userFullName, userEmail, userPhoneNum, userRole) //SEND THE FORM 
                }
            } else { 
                $.toast({
                    heading: 'Failed',
                    text: 'Email không hợp lệ!',
                    showHideTransition: 'slide',
                    position : "top-right",
                    icon: 'error',
                    hideAfter: 3000
                })
            }
            //RETURN OLD FORM
            $(this).html("Sửa") 
            $form = $(this).closest("tr"),
            $form.children("td.userFullName").removeAttr("contenteditable"),
            $form.children("td.userEmail").removeAttr("contenteditable"),
            $form.children("td.userPhoneNum").removeAttr("contenteditable"),
            $form.children("td.userRole").empty().html(userRole)
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
    function processAllUsers() {
        getAllUsers().done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAllUsers()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else {
                appendUserInfo(result)
            }
        })
    }
    function getAllUsers() {
        return $.ajax({
            method: "GET",
            url: "http://localhost:8080/MyCRM/api/user",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token"),
            },
        })
    }
    function appendUserInfo(result) {
        result = result.content;
        $("#example tbody").empty()
        $.each(result, function(index, value){
            var row =
            `<tr>
                <td class="userId">${value.id}</td>
                <td class="userFullName">${value.fullName}</td>
                <td class="userEmail">${value.email}</td>
                <td class="userPhoneNum">${value.phoneNum}</td>
                <td class="userRole">${value.role}</td>
                <td>
                    <a class="btn btn-sm btn-primary btn-update">Sửa</a>
                    <a class="btn btn-sm btn-danger btn-delete" user-id="${value.id}">Xóa</a>
                    <a href="user-details.html?${value.id}" class="btn btn-sm btn-info">Xem</a>
                </td>
            </tr>`
            $("#example tbody").append(row)
        })
        $("#example").dataTable({
            pageLength : 5 //set number of rows
        })
    }
    function processDeleteUser(userId) {
        deleteUser(userId).done(function(result) {
            if (result.statusCode == 401) { 
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processDeleteUser(userId)
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
                //handle if it's 403 status
                $(".btn-delete").html("Xóa")
            } else { // if access_token is valid
                showDeleteResult(result)
            }
        })
    }
    function deleteUser(userId) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/user/delete",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : {
                id : userId
            },
            contentType : "application/x-www-form-urlencoded"
        })
    }
    function showDeleteResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Xóa thành viên thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $('#example').DataTable().destroy()
            processAllUsers()
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Xóa thành viên thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
    function processGetRoleOptions($select, oldRole) {
        getAllRoles().done(function(result) { //GET ALL ROLES
            if (result.statusCode == 401) { //if access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetRoleOptions($select, oldRole)
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
                //handle if it's 403 status
                $(".btn-update").html("Sửa")
                $form = $(".btn-update").closest("tr"),
                $form.children("td.userRole").empty().html(oldRole)
            } else { // if access_token is valid 
                appendRoleOptions(result, $select, oldRole)
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
    function appendRoleOptions(result, $select, oldRole) {
        result = result.content;
        $.each(result, function(index, value) {
            var option;
            if (oldRole == value.name) {
                option = `<option value='${value.name}' selected>${value.name}</option>`
            } else {
                option = `<option value='${value.name}'>${value.name}</option>`
            }
            $select.append(option)
        })
    }
    function processUpdateUser(userId, userFullName, userEmail, userPhoneNum, userRole) {
        updateUser(userId, userFullName, userEmail, userPhoneNum, userRole).done(function(result, textStatus, jqXHR) {
            if (result.statusCode == 401) { //if access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateUser(userId, userFullName, userEmail, userPhoneNum, userRole)
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
            } else { // if access_token is valid 
                showUpdateResult(result, textStatus, jqXHR)
            }
        })
    }
    function updateUser(userId, userFullName, userEmail, userPhoneNum, userRole) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/user/update",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : JSON.stringify({
                id : userId,
                fullName : userFullName,
                email : userEmail,
                phoneNum : userPhoneNum,
                role : userRole
            }),
            contentType: "application/json"
        })
    }
    function showUpdateResult(result, textStatus, jqXHR) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Sửa thành viên thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Sửa thành viên thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
});
