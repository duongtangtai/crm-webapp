$(document).ready(function() {
    var href = $(location).attr("href")
    if (href.indexOf("?") < 0 || href.indexOf("?") == href.length - 1) {
        location.replace("profile-task.html") //go back to profile-task cz userId is invalid
    } else {
        id = href.substring(href.indexOf("?") + 1)
        processProfileTaskById(id)
    }
    //-------------------------------------UPDATE BUTTON EVENT----------------------------------
    $("#update-form").submit(function(event) { //staff can only update status and note
        event.preventDefault()
        if (confirm("Xác nhận cập nhật công việc này?")) {
            taskId = id; 
            var $form = $(this),
            taskStatusName = $form.find("select[name='taskStatusName']").val(),
            taskNote = $form.find("textarea[name='taskNote']").val()
            processUpdateProfileTask(taskId, taskStatusName, taskNote)
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
    function processProfileTaskById(id) {
        getProfileTaskById(id).done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processProfileTaskById(id)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode != undefined && !result.successful) { 
                location.replace("profile-task.html") //go back to profile-task cz userId is invalid
            } else {
                appendProfileTaskInfo(result)
            }
        })
    }
    function getProfileTaskById(id) {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/profile-task/" + id,
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendProfileTaskInfo(result) {
        result = result.content;
        var $form = $("#update-form")
        $form.find("input[name='taskName']").attr("value",result.name),
        $form.find("input[name='taskJobName']").attr("value",result.jobName),
        $form.find("input[name='taskUserName']").attr("value",result.userName),
        $form.find("input[name='taskStartDate']").attr("value",result.startDate),
        $form.find("input[name='taskEndDate']").attr("value",result.endDate),
        $form.find("textarea[name='taskNote']").text(result.note)
        $statusSelect = $form.find("select[name='taskStatusName']")
        processGetStatusOptions($statusSelect, result.statusName)
    }
    function processGetStatusOptions($statusSelect, statusName) {
        //-------------GET STATUS OPTIONS------------
        getAllStatus().done(function(result) {
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetStatusOptions()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else { //append result if succeeded
                appendStatusOptions(result, $statusSelect, statusName)
            }
        })
    }
    function getAllStatus() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/status",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendStatusOptions(result, $statusSelect, statusName) {
        result = result.content;
        $.each(result, function(index, value) {
            var statusOption;
            if (value.name == statusName) {
                statusOption = `<option value="${value.name}" selected>${value.name}</option>`
            } else {
                statusOption = `<option value="${value.name}">${value.name}</option>`
            }
            $statusSelect.append(statusOption)
        })
    }
    function processUpdateProfileTask(taskId, taskStatusName, taskNote) {
        updateProfileTask(taskId, taskStatusName, taskNote).done(function(result) {
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateProfileTask(taskId, taskStatusName, taskNote)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else { //append result if succeeded
                showUpdateProfileTaskResult(result)
            }
        })
    }
    function updateProfileTask(taskId, taskStatusName, taskNote) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/profile-task",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : JSON.stringify({
                id : taskId,
                statusName : taskStatusName,
                note : taskNote
            }),
            contentType : "application/json"
        })
        
    }
    function showUpdateProfileTaskResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Cập nhật công việc thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Cập nhật công việc thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})
