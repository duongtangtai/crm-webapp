$(document).ready(function(){
    //send sth to server to check authorization
    processAJob()
    //---------------------------ADD BUTTON EVENT-----------------------------------------
    $("#add-form").submit(function(event){ 
        event.preventDefault()
        if (confirm("Xác nhận thêm dự án này?")) {
            var $form = $(this), //get values from the form
            jobName = $form.find("input[name='jobName']").val(),
            jobStartDate = $form.find("input[name='jobStartDate']").val(),
            jobEndDate = $form.find("input[name='jobEndDate']").val()
            processAddJob($form, jobName, jobStartDate, jobEndDate)
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
    function processAJob() {
        getAJob().done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAJob()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else {
                //do nothing
            }
        })
    }
    function getAJob() {
        return $.ajax({
            method:"GET",
            url:"http://localhost:8080/MyCRM/api/job/1",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function processAddJob($form, jobName, jobStartDate, jobEndDate) {
        addJob(jobName, jobStartDate, jobEndDate).done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAddJob($form, jobName, jobStartDate, jobEndDate)
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else {
                showAddResult(result, $form)
            }
        })
    }
    function addJob(jobName, jobStartDate, jobEndDate) {
        return $.ajax({
            method:"POST",
            url:"http://localhost:8080/MyCRM/api/job/add",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data:JSON.stringify({
                name : jobName,
                startDate : jobStartDate,
                endDate : jobEndDate
            }),
            contentType: "application/json"
        })
    }
    function showAddResult(result, $form) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Thêm thành viên thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
            $form.find("input[name='jobName']").val("")
            $form.find("input[name='jobStartDate']").val("")
            $form.find("input[name='jobEndDate']").val("")
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Thêm dự án thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})