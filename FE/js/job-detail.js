$(document).ready(function() {
    var href = $(location).attr("href")
    if (href.indexOf("?") < 0 || href.indexOf("?") == href.length - 1) {
        location.replace("job-table.html") //go back to usertable cz userId is invalid
    } else {
        id = href.substring(href.indexOf("?") + 1)
        processJobDetailById(id)
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
    function processJobDetailById(id) {
        getJobDetailById(id).done(function(result) {
            if (result.statusCode == 401) { //access_token is invalid
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processJobDetailById(id)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else if (result.content == null) { //invalid jobID
                location.replace("job-table.html")
            } else {
                appendJobDetail(result)
            }
        })
    }
    function getJobDetailById(id) {
        return $.ajax({ //Get user information
            method : "GET",
            url : "http://localhost:8080/MyCRM/api/job/"+id,
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token") 
            }
        })
    }
    function appendJobDetail(result) {
        //----------------STATISTICS-------------------
        result = result.content;
        var notStarted = 0, progressing = 0 , finished = 0
        $.each(result.statistics, function(index, value) {
            switch(index) {
                case "Chưa bắt đầu":
                    notStarted = value
                    break;
                case "Đang thực hiện":
                    progressing = value;
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
        //----------------USER DETAIL & RELATIVE TASK-------------------
        $("#user-detail").empty()
        $.each(result.userModelList, function(index, value){
            var userId = value.id
            var userDetail = 
            `
                <div class="col-xs-12">
                    <a href="user-details.html?${userId}" class="group-title">
                        <img width="30" height="30" src="plugins/images/users/pawandeep.jpg" 
                        id="avatar-${userId}" class="img-circle"/>
                        <span>${value.fullName}</span>
                    </a>
                </div>
                <div class="col-md-4">
                    <div class="white-box">
                        <h3 class="box-title">Chưa thực hiện</h3>
                        <div class="message-center" id="user-not-started-${userId}"></div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="white-box">
                        <h3 class="box-title">Đang thực hiện</h3>
                        <div class="message-center" id="user-progressing-${userId}"></div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="white-box">
                        <h3 class="box-title">Đã hoàn thành</h3>
                        <div class="message-center" id="user-finished-${userId}"></div>
                    </div>
                </div>
            `
            $("#user-detail").append(userDetail)
            var userAvatar = value.avatar
            if (userAvatar!=null) {
                $("#avatar-"+userId).attr("src",userAvatar)
            }
            $.each(value.taskList, function(index, value) {
                var task =
                `
                <a href="task-details.html?${value.id}">
                    <div class="mail-contnet">
                        <h5>${value.name}</h5>
                        <span class="time">Ngày bắt đầu: ${value.startDate}</span>
                        <span class="time">Ngày kết thúc: ${value.endDate}</span>
                    </div>
                </a>
                `
                switch (value.statusName) {
                    case "Chưa bắt đầu":
                        $("#user-not-started-"+userId).append(task)
                        break;
                    case "Đang thực hiện":
                        $("#user-progressing-"+userId).append(task)
                        break;
                    case "Đã hoàn thành":
                        $("#user-finished-"+userId).append(task)
                        break;
                }
            })
            $("#user-not-started-"+userId).html().length == 0 ? $("#user-not-started-"+userId).html("Không có công việc") : ""
            $("#user-progressing-"+userId).html().length == 0 ? $("#user-progressing-"+userId).html("Không có công việc") : ""
            $("#user-finished-"+userId).html().length == 0 ? $("#user-finished-"+userId).html("Không có công việc") : ""
        })
    }
})