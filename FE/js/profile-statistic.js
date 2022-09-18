$(document).ready(function() {
    //call api to get profile
    processUserProfileStatistic()
    //----------------------------------UPDATE BUTTON EVENT-------------------------------------
    $("body").on("click",".btn-update", function() {
        location.replace("profile-task-edit.html?"+$(this).attr("task-id"))
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
    function processUserProfileStatistic() {
        getUserProfileStatistic().done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUserProfileStatistic()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else {
                appendUserProfile(result)
            }
        })
    }   
    function getUserProfileStatistic() {
        return $.ajax({
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/profile-statistic",
            headers : { 
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendUserProfile(result) {
        result = result.content;
        //----------------USER INFO--------------------
        $("#fullName").html(result.fullName)
        $("#email").html(result.email)
        //----------------STATISTICS-------------------
        var notStarted = 0, progressing = 0 , finished = 0
        $.each(result.statistics, function(index, value) {
            switch(index) {
                case "Chưa bắt đầu":
                    notStarted = value
                    break;
                case "Đang thực hiện":
                    progressing = value
                    break;
                case "Đã hoàn thành":
                    finished = value
                    break;
            }
        })
        var total = notStarted + progressing + finished
        $("#not-started").html(Math.round((notStarted/total)*100)+"%")
        $("#not-started-bar").attr("style","width:"+Math.round((notStarted/total)*100)+"%")
        $("#progressing").html(Math.round((progressing/total)*100)+"%")
        $("#progressing-bar").attr("style","width:"+Math.round((progressing/total)*100)+"%")
        $("#finished").html(Math.round((finished/total)*100)+"%")
        $("#finished-bar").attr("style","width:"+Math.round((finished/total)*100)+"%")
        notStarted == 0 ? $("#not-started").html("0%") : ""
        progressing == 0 ? $("#progressing").html("0%") : ""
        finished == 0 ? $("#finished").html("0%") : ""
        //----------------TASK DETAIL-------------------
        $("#example tbody").empty()
        $.each(result.taskList, function(index, value){ //WE DONT HAVE JOB NAME!!!! 
            var row = 
            `<tr>
                <td class="taskId">${value.id}</td>
                <td class="taskName">${value.name}</td>
                <td class="taskJobName">${value.jobName}</td>
                <td class="taskStartDate">${value.startDate}</td>
                <td class="taskEndDate">${value.endDate}</td>
                <td class="taskStatusName">${value.statusName}</td>
                <td>
                    <a class="btn btn-sm btn-primary btn-update" task-id=${value.id}>Cập nhật</a>
                </td>
            </tr>`
            $("#example tbody").append(row)
        })
        $("#example").dataTable()
    }
})
