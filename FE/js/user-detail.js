$(document).ready(function() {
    var href = $(location).attr("href")
    if (href.indexOf("?") < 0 || href.indexOf("?") == href.length - 1) {
        location.replace("user-table.html") //go back to usertable cz userId is invalid
    } else {
        id = href.substring(href.indexOf("?") + 1)
        processUserDetailById(id)
    }
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
    function processUserDetailById(id) {
        getUserById(id).done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processGetUserDetailById(id)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else if (result.content == null) { //invalid userId
                location.replace("user-table.html")
            } else {
                appendUserDetail(result)
            }
        })
    }
    function getUserById(id) {
        return $.ajax({ //Get user information
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/user/"+id,
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendUserDetail(result) {
        result = result.content;
        //----------------USER INFO--------------------
        $("#fullName").html(result.fullName)
        $("#email").html(result.email)
        if (result.avatar != null) {
            $("#avatar").attr("src",result.avatar)
        }
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
        $.each(result.taskList, function(index, value) {
            var task =                                
            `<a href="task-details.html?${value.id}">
                <div class="mail-contnet">
                    <h5>${value.name}</h5>
                    <span class="mail-desc"></span>
                    <span class="time">Ngày bắt đầu: ${value.startDate}</span>
                    <span class="time">Ngày kết thúc: ${value.endDate}</span>
                </div>
            </a>`
            switch (value.statusName) {
                case "Chưa bắt đầu":
                    $("#not-started-task").append(task)
                    break;
                case "Đang thực hiện":
                    $("#progressing-task").append(task)
                    break;
                case "Đã hoàn thành":
                    $("#finished-task").append(task)
                    break;
            }
        })
        $("#not-started-task").html().length == 0 ? $("#not-started-task").html("Không có công việc") : ""
        $("#progressing-task").html().length == 0 ? $("#progressing-task").html("Không có công việc") : ""
        $("#finished-task").html().length == 0 ? $("#finished-task").html("Không có công việc") : ""
    }
})
