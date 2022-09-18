$(document).ready(function(){
    //----------------------GET ALL OPTIONS-------------------------
    processGetJobOptions(processGetUserOptions(processGetStatusOptions()))
    //----------------------ADD BUTTON EVENT------------------------
    $("#add-form").submit(function(event) {
        event.preventDefault()
        if (confirm("Xác nhận thêm công việc này?")) {
            var $form = $(this),
            taskJobName = $form.find("select[name='taskJobName']").val(),
            taskName = $form.find("input[name='taskName']").val(),
            taskUserName = $form.find("select[name='taskUserName']").val(),
            taskStatusName = $form.find("select[name='taskStatusName']").val(),
            taskStartDate = $form.find("input[name='taskStartDate']").val(),
            taskEndDate = $form.find("input[name='taskEndDate']").val()
            processAddTask($form, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, taskEndDate)
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
    function processGetJobOptions(callback = () => {}) {
        //-------------GET JOB OPTIONS------------
        getAllJobs().done(function(result) {
            if (result.statusCode == 401) { 
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetJobOptions(callback)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else { //append result if succeeded
                appendJobOptions(result)
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
    function appendJobOptions(result) {
        result = result.content;
        $.each(result, function(index, value) {
            $("#job-options select").append(`<option>${value.name}</option>`)
        })
    }
    function processGetUserOptions(callback = () => {}) {
        //-------------GET USER OPTIONS------------
        getAllUsers().done(function(result) {
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetUserOptions(callback)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else { //append result if succeeded
                appendUserOptions(result)
                callback()
            }
        })
    }
    function getAllUsers() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/user",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendUserOptions(result) {
        result = result.content;
        $.each(result, function(index, value) {
            $("#user-options select").append(`<option>${value.fullName}</option>`)
        })
    }
    function processGetStatusOptions() {
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
                appendStatusOptions(result)
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
    function appendStatusOptions(result) {
        result = result.content;
        $.each(result, function(index, value) {
            $("#status-options select").append(`<option>${value.name}</option>`)
        })
    }
    function processAddTask($form, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, taskEndDate) {
        addTask(taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, taskEndDate).done(function(result) {
            if (result.statusCode == 401) { //reload page so it will refresh the token
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAddTask($form, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, taskEndDate)
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
    function addTask(taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, taskEndDate) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/task/add",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : JSON.stringify({
                name : taskName,
                jobName : taskJobName,
                userName : taskUserName,
                statusName : taskStatusName,
                startDate : taskStartDate,
                endDate : taskEndDate
            }),
            contentType : "application/json"
        })
    }
    function showAddResult(result, $form) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Thêm công việc thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $form.find("select[name='taskJobName']").val("")
            $form.find("input[name='taskName']").val(""),
            $form.find("select[name='taskUserName']").val(""),
            $form.find("select[name='taskStatusName']").val(""),
            $form.find("input[name='taskStartDate']").val(""),
            $form.find("input[name='taskEndDate']").val("")
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Thêm công việc thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})