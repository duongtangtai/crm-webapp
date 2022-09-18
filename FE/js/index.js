$(document).ready(function(){
    processAllTasks()
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
            } else {
                appendTaskStatistics(result)
            }
        })
    }
    function getAllTasks() {
        return $.ajax({
            method:"GET",
            url:"http://localhost:8080/MyCRM/api/dashboard",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendTaskStatistics(result) {
        result = result.content;
        var notStarted = 0, progressing = 0 , finished = 0
        $.each(result, function(index, value) {
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
        $("#not-started").html(notStarted)
        $("#not-started-bar").attr("style","width:"+Math.round((notStarted/total)*100)+"%")
        $("#progressing").html(progressing)
        $("#progressing-bar").attr("style","width:"+Math.round((progressing/total)*100)+"%")
        $("#finished").html(finished)
        $("#finished-bar").attr("style","width:"+Math.round((finished/total)*100)+"%")
        notStarted == 0 ? $("#not-started").html("0") : ""
        progressing == 0 ? $("#progressing").html("0") : ""
        finished == 0 ? $("#finished").html("0") : ""
    }
})