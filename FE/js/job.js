$(document).ready(function(){
    //-----------------------------------------FILL JOB INFO--------------------------------------------
    processAllJobs()
    //--------------------------------DELETE BUTTON-----------------------------------
    $("body").on("click",".btn-delete",function() {
        if (confirm("Xác nhận xóa dự án này?")) {
            var jobId =  $(this).attr("job-id")
            processDeleteJob(jobId)
        }
    })
    //--------------------------------UPDATE BUTTON-----------------------------------
    $("body").on("click",".btn-update",function(){
        if ($(this).text() == "Sửa") {
            $(this).html("Cập nhật")
            $form = $(this).closest("tr"), //LET CLIENT EDIT INFORMATION
            $form.children("td.jobName").attr("contenteditable",""),
            $startDate = $form.children("td.jobStartDate"),
            $endDate = $form.children("td.jobEndDate")
            var startDate = $startDate.text() 
            var endDate = $endDate.text()
            $startDate.empty().html(`<input type="date" value=${startDate}>`)
            $endDate.empty().html(`<input type="date" value=${endDate}>`)
        } else if ($(this).text() == "Cập nhật") {
            var $form = $(this).closest("tr"), //CREATE A FORM
            jobId = $form.children("td.jobId").text(),
            jobName = $form.children("td.jobName").text(),
            jobStartDate = $form.children("td.jobStartDate").children("input").val(),
            jobEndDate = $form.children("td.jobEndDate").children("input").val()
            if (confirm("Xác nhận sửa dự án này?")) { 
                processUpdateJob(jobId, jobName, jobStartDate, jobEndDate)
            }
            $(this).html("Sửa") //RETURN OLD FORM
            $form = $(this).closest("tr"),
            $form.children("td.jobName").removeAttr("contenteditable"),
            $form.children("td.jobStartDate").empty().html(jobStartDate),
            $form.children("td.jobEndDate").empty().html(jobEndDate)
        }
    })
    //----------------------------------FUNCTIONS-----------------------------------
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
    function processAllJobs() {
        getAllJobs().done(function(result){
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processAllJobs()
                    } else { //if refresh_token is expired. The client have to login again
                        location.replace("login.html")//after log-in the client will have two brand new tokens!!
                    }
                })
            } else if (result.statusCode == 403) {
                location.replace("403.html")
            } else {
                appendJobInfo(result)
            }
        })
    }
    function getAllJobs() {
        return $.ajax({
            method:"GET",
            url:"http://localhost:8080/MyCRM/api/job",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            }
        })
    }
    function appendJobInfo(result) {
        result = result.content;
        $("#example tbody").empty()
        $.each(result, function(index, value){
        var row = 
            `<tr>
                <td class="jobId">${value.id}</td>
                <td class="jobName">${value.name}</td>
                <td class="jobStartDate">${value.startDate}</td>
                <td class="jobEndDate">${value.endDate}</td>
                <td>
                    <a class="btn btn-sm btn-primary btn-update">Sửa</a>
                    <a class="btn btn-sm btn-danger btn-delete" job-id="${value.id}">Xóa</a>
                  <a href="job-details.html?${value.id}" class="btn btn-sm btn-info">Xem</a>
                </td>
            </tr>`
            $("#example tbody").append(row)
        })
        $("#example").dataTable({
            pageLength : 5 //set number of rows
        })
    }
    function processDeleteJob(jobId) {
        deleteJob(jobId).done(function(result) {
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processDeleteJob(jobId)
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
    function deleteJob(jobId) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/job/delete",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : {
                id : jobId
            },
            contentType : "application/x-www-form-urlencoded"
        })
    }
    function showDeleteResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Xóa dự án thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })                    
            $('#example').DataTable().destroy()
            processAllJobs()
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Xóa dự án thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
    function processUpdateJob(jobId, jobName, jobStartDate, jobEndDate) {
        updateJob(jobId, jobName, jobStartDate, jobEndDate).done(function(result) {
            if (result.statusCode == 401) {
                refreshToken().done(function(result, textStatus, jqXHR){
                    if (result.successful) { //if refresh_token is still valid, get another access_token 
                        saveTokens(jqXHR)
                        processUpdateJob(jobId, jobName, jobStartDate, jobEndDate)
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
    function updateJob(jobId, jobName, jobStartDate, jobEndDate) {
        return $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/job/update",
            headers : {
                "Authorization" : "Bearer " + localStorage.getItem("access_token")
            },
            data : JSON.stringify({
                id : jobId,
                name : jobName,
                startDate : jobStartDate,
                endDate : jobEndDate
            }),
            contentType : "application/json"
        })
    }
    function showUpdateResult(result) {
        if (result.successful) {
            $.toast({
                heading: 'Succeeded',
                text: 'Sửa dự án thành công!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'success',
                hideAfter: 3000
            })
        } else {
            $.toast({
                heading: 'Failed',
                text: 'Sửa dự án thất bại!',
                showHideTransition: 'slide',
                position : "top-right",
                icon: 'error',
                hideAfter: 3000
            })
        }
    }
})
