$(document).ready(function() {
    //---------------------------FILL ROLE INFO----------------------------
    processAllRoles()
    //--------------------------DELETE BUTTON EVENT---------------------------
    $("body").on("click",".btn-delete",function(){ //Đăng ký click event sau khi có tbody
        if (confirm("Xác nhận xóa quyền này?")) {
            var roleId = $(this).attr("role-id")
            processDeleteRole(roleId)
        }
    })
    //--------------------------------UPDATE BUTTON-----------------------------------
    $("body").on("click",".btn-update",function() { 
        if ($(this).text() == "Sửa") {
            $(this).html("Cập nhật")
            $form = $(this).closest("tr"),
            $form.children("td.roleName").attr("contenteditable",""),
            $form.children("td.roleDescript").attr("contenteditable","")
        } else if ($(this).text() == "Cập nhật") {
            if (confirm("Xác nhận sửa quyền này?")) {//CREATE A FORM TO SEND
                var $form = $(this).closest("tr"),
                roleId = $form.children("td.roleId").text(),
                roleName = $form.children("td.roleName").text(),
                roleDescript = $form.children("td.roleDescript").text()
                processUpdateRole(roleId, roleName, roleDescript)
            }
            $(this).html("Sửa")
            $form = $(this).closest("tr"),
            $form.children("td.roleName").removeAttr("contenteditable"),
            $form.children("td.roleDescript").removeAttr("contenteditable")
        }
    })
    //------------------------------FUNTIONS--------------------------------
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
    function processAllRoles() {
        getAllRoles().done(function(result){ // after ajax() we'll get a result, then we pass it to another function
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAllRoles()
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
            method : "GET", //Phương thức tương ứng với link
            url : "http://localhost:8080/MyCRM/api/role", //Link lấy danh sách role
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendRoleOptions(result) {
        result = result.content;
        $("#example tbody").empty() //go to id=example -> go to <tbody> -> empty()
        $.each(result, function(index, value) { //loop through an array
            var row =
            `<tr>
                <td class="roleId">${value.id}</td>
                <td class="roleName">${value.name}</td>
                <td class="roleDescript">${value.description}</td>
                <td>
                   <a href="#" class="btn btn-sm btn-primary btn-update">Sửa</a>
                   <a href="#" class="btn btn-sm btn-danger btn-delete" role-id="${value.id}">Xóa</a>
                </td>
            </tr>`
            $("#example tbody").append(row)
        })
        $("#example").dataTable({
            pageLength : 5 //set number of rows
        })
    }
    function processDeleteRole(roleId) {
        deleteRole(roleId).done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processDeleteRole(roleId)
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
                showDeleteResult(result)
            }
        })
    }
    function deleteRole(roleId) {
        return $.ajax({
            method:"POST",
            url:"http://localhost:8080/MyCRM/api/role/delete",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            },
            data:{ // x-form-urlencoded type
                id : roleId // key - value
            },
            contentType: "application/x-www-form-urlencoded"
        })
    }
    function showDeleteResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Xóa quyền thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $('#example').DataTable().destroy()
            processAllRoles()
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Xóa quyền thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
    function processUpdateRole(roleId, roleName, roleDescript) {
        updateRole(roleId, roleName, roleDescript).done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateRole(roleId, roleName, roleDescript)
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
                showUpdateResult(result)
            }
        })
    }
    function updateRole(roleId, roleName, roleDescript) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/role/update",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            },
            data : JSON.stringify({ // turn into json object
                id : roleId,
                name : roleName,
                description : roleDescript
            }),
            contentType: "application/json"
        })
    }
    function showUpdateResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Sửa quyền thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })                    
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Sửa quyền thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
});