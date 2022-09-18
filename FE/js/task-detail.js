$(document).ready(function() {
    var href = $(location).attr("href")
    if (href.indexOf("?") < 0 || href.indexOf("?") == href.length - 1) {
        location.replace("task-table.html") //go back to usertable cz userId is invalid
    } else {
        id = href.substring(href.indexOf("?") + 1)
        processGetTaskById(id)
    }
    //------------------------------------UPDATE BUTTON EVENT----------------------------------
    $("#update-form").submit(function(event) {
        event.preventDefault()
        if (confirm("Xác nhận cập nhật công việc này?")) {
            taskId = id;
            var $form = $(this),
            taskName = $form.find("input[name='taskName']").val(),
            taskJobName = $form.find("select[name='taskJobName']").val(),
            taskUserName = $form.find("select[name='taskUserName']").val(),
            taskStatusName = $form.find("select[name='taskStatusName']").val(),
            taskStartDate = $form.find("input[name='taskStartDate']").val(),
            taskEndDate = $form.find("input[name='taskEndDate']").val(),
            taskNote = $form.find("textarea[name='taskNote']").val()
            processUpdateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, 
                taskStartDate, taskEndDate, taskNote)
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
    function processGetTaskById(id) {
        getTaskById(id).done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetTaskById(id)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else if (result.content == null) { //invalid taskId
                location.replace("task-table.html")
            } else {
                appendUserDetail(result)
            }
        })
    }
    function getTaskById(id) {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/task/" + id,
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendUserDetail(result) {
        result = result.content;
        var $form = $("#update-form")
        $form.find("input[name='taskName']").attr("value",result.name),
        $form.find("input[name='taskStartDate']").attr("value",result.startDate),
        $form.find("input[name='taskEndDate']").attr("value",result.endDate),
        $form.find("textarea[name='taskNote']").text(result.note)
        //select options for job, user and status
        $jobSelect = $form.find("select[name='taskJobName']")
        $userSelect = $form.find("select[name='taskUserName']")
        $statusSelect = $form.find("select[name='taskStatusName']")
        processGetJobOptions($jobSelect, result.jobName, 
            processGetUserOptions($userSelect, result.userName,
                processGetStatusOptions($statusSelect, result.statusName)))
    }
    function processGetJobOptions($jobSelect, jobName, callback = () => {}) {
        //-----------HANDLE JOB OPTIONS-------------
        getAllJobs().done(function(result) {
            if (result.statusCode == 401) { //if access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetJobOptions($jobSelect, jobName, callback)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else { // if access_token is valid 
                appendJobOptions(result, $jobSelect, jobName)
                callback()
            }
        })
    }
    function getAllJobs() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/job",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendJobOptions(result, $jobSelect, jobName) {
        result = result.content;
        $.each(result,function(index, value) {
            var jobOption;
            if (value.name == jobName) {
                jobOption = `<option value="${value.name}" selected>${value.name}</option>`
            } else {
                jobOption = `<option value="${value.name}">${value.name}</option>`
            }
            $jobSelect.append(jobOption);
        })
    }
    function processGetUserOptions($userSelect, userName, callback = () => {}) {
        //-----------HANDLE USER OPTIONS-------------
        getUserOptions().done(function(result) {
            if (result.statusCode == 401) { //if access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetUserOptions($userSelect, userName, callback)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else { // if access_token is valid 
                appendUserOptions(result, $userSelect, userName)
                callback()
            }
        })
    }
    function getUserOptions() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/user",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendUserOptions(result, $userSelect, userName) {
        result = result.content;
        $.each(result, function(index, value) {
            var userOption;
            if (value.fullName == userName) {
                userOption = `<option value="${value.fullName}" selected>${value.fullName}</option>`
            } else {
                userOption = `<option value="${value.fullName}">${value.fullName}</option>`
            }
            $userSelect.append(userOption)
        })
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
    function processUpdateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
        taskEndDate, taskNote) {
        updateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
            taskEndDate, taskNote).done(function(result) {
            if (result.statusCode == 401) { //invalid token
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, 
                            taskStartDate, taskEndDate, taskNote)
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
    function updateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, 
        taskStartDate, taskEndDate, taskNote) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/task/update",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : JSON.stringify({
                id : taskId,
                name : taskName,
                jobName : taskJobName,
                userName : taskUserName,
                startDate : taskStartDate,
                endDate : taskEndDate,
                statusName : taskStatusName,
                note : taskNote
            }),
            contentType : "application/json"
        })
    }
    function showUpdateResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Sửa công việc thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Sửa công việc thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})