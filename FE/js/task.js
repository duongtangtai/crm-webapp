$(document).ready(function(){
    //-------------------------------FILL TASK INFO-----------------------------------
    processAllTasks()
    //--------------------------------DELETE BUTTON-----------------------------------
    $("body").on("click",".btn-delete",function(){
        if (confirm("Xác nhận xóa công việc này?")) {
            var taskId = $(this).attr("task-id")
            processDeleteTask(taskId)
        }
    })
    //--------------------------------UPDATE BUTTON-----------------------------------
    $("body").on("click",".btn-update", function() {
        if ($(this).text() == "Sửa") {
            $(this).html("Cập nhật")
            $form = $(this).closest("tr")
            $form.children("td.taskName").attr("contenteditable",""), //can be freely edited
            $jobName = $form.children("td.taskJobName"), //the following 5 must follow some options
            $userName = $form.children("td.taskUserName"),
            $startDate = $form.children("td.taskStartDate"),
            $endDate = $form.children("td.taskEndDate"),
            $statusName = $form.children("td.taskStatusName")
            var jobName = $jobName.text()
            $jobName.empty().html(`<select></select>`)
            $jobSelect = $jobName.children("select")
            var userName = $userName.text()
            $userName.empty().html(`<select></select>`)
            $userSelect = $userName.children("select")
            var statusName = $statusName.text()
            $statusName.empty().html(`<select></select>`)
            $statusSelect = $statusName.children("select")
            //GET ALL OPTIONS
            processGetJobOptions($jobSelect, jobName, 
                processGetUserOptions($userSelect, userName,
                    processGetStatusOptions($statusSelect, statusName, 
                        processHandleDate($startDate, $endDate))))
        } else if ($(this).text() == "Cập nhật") {
            var $form = $(this).closest("tr"), //CREATE A FORM TO SEND
            taskId = $form.children("td.taskId").text(),
            taskName = $form.children("td.taskName").text(),
            taskJobName = $form.children("td.taskJobName").children("select").val(),
            taskUserName = $form.children("td.taskUserName").children("select").val(),
            taskStartDate = $form.children("td.taskStartDate").children("input").val(),
            taskEndDate = $form.children("td.taskEndDate").children("input").val(),
            taskStatusName = $form.children("td.taskStatusName").children("select").val(),
            taskNote = $form.children("td.taskNote").text()
            if (confirm("Xác nhận sửa công việc này?")) {  //SEND THE FORM
                processUpdateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
                    taskEndDate, taskNote)
            }
            $(this).html("Sửa") //RETURN OLD FORM
            $form = $(this).closest("tr"),
            $form.children("td.taskName").removeAttr("contenteditable"),
            $form.children("td.taskJobName").empty().html(taskJobName),
            $form.children("td.taskUserName").empty().html(taskUserName),
            $form.children("td.taskStartDate").empty().html(taskStartDate),
            $form.children("td.taskEndDate").empty().html(taskEndDate),
            $form.children("td.taskStatusName").empty().html(taskStatusName)
        }
    })
    //--------------------------------------FUNCTIONS--------------------------------------------
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
    function processAllTasks() {
        getAllTasks().done(function(result){
            if (result.statusCode == 401) { //invalid token  
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAllTasks()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else {
                appendTaskInfo(result)
            }
        })
    }
    function getAllTasks() {
        return $.ajax({
            method:"GET",
            url:"http://localhost:8080/MyCRM/api/task",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendTaskInfo(result) {
        result = result.content;
        $("#example tbody").empty()
        $.each(result, function(index, value){
            var row = 
            `<tr>
                <td class="taskId">${value.id}</td>
                <td class="taskName">${value.name}</td>
                <td class="taskJobName">${value.jobName}</td>
                <td class="taskUserName">${value.userName}</td>
                <td class="taskStartDate">${value.startDate}</td>
                <td class="taskEndDate">${value.endDate}</td>
                <td class="taskStatusName">${value.statusName}</td>
                <td class="taskNote" hidden>${value.note}</td>
                <td>
                    <a class="btn btn-sm btn-primary btn-update">Sửa</a>
                    <a class="btn btn-sm btn-danger btn-delete" task-id=${value.id}>Xóa</a>
                    <a href="task-details.html?${value.id}"class="btn btn-sm btn-info" >Xem</a>
                </td>
            </tr>`
            $("#example tbody").append(row)
        })
        $("#example").dataTable({
            pageLength : 5
        })
    }
    function processDeleteTask(taskId) {
        deleteTaskById(taskId).done(function(result) {
            if (result.statusCode == 401) { //invalid token
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processDeleteTask(taskId)
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
    function deleteTaskById(taskId) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/task/delete",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : {
                id : taskId
            },
            contentType : "application/x-www-form-urlencoded"
        })
    }
    function showDeleteResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Xóa công việc thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $('#example').DataTable().destroy()
            processAllTasks()
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Xóa công việc thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
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
    function processGetStatusOptions($statusSelect, statusName, callback = () => {}) {
        //-----------HANDLE STATUS OPTIONS-------------
        getAllStatus().done(function(result) {
            if (result.statusCode == 401) { //if access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetStatusOptions($statusSelect, statusName, callback)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else { // if access_token is valid 
                appendStatusOptions(result, $statusSelect, statusName)
                callback()
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
    function appendStatusOptions(result,  $statusSelect, statusName) {
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
    function processHandleDate($startDate, $endDate) {
        //-----------HANDLE DATE-------------
        var startDate = $startDate.text(), endDate = $endDate.text()
        $startDate.empty().html(`<input type="date" value=${startDate}>`)
        $endDate.empty().html(`<input type="date" value=${endDate}>`)
    }
    function processUpdateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
        taskEndDate, taskNote) {
        updateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
            taskEndDate, taskNote).done(function(result) {
            if (result.statusCode == 401) { //invalid token
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
                            taskEndDate, taskNote)
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
    function updateTask(taskId, taskName, taskJobName, taskUserName, taskStatusName, taskStartDate, 
        taskEndDate, taskNote) {
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