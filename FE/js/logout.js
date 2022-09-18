$(document).ready(function() {//we can simply resolve username and avatar in this js
    var fullName = localStorage.getItem("fullName"),
    avatar = localStorage.getItem("avatar")
    //loading avatar and userFullName
    avatar != null ? $("#myAvatar").attr("src",avatar) : ""
    $("#myName").html(fullName)
    // register an event for the log-out button
    $("body").on("click","#logout",function() { 
        $.ajax({
            method : "POST",
            url : "http://localhost:8080/MyCRM/api/auth/logout",
            headers : { //use refresh token to make sure it's valid -> we erase them in DB anyway
                "Authorization" : "Bearer " + localStorage.getItem("refresh_token") 
            }
        }).done(function(result){ //whatever the result it, just clear localStorage and redirect to login.html
            localStorage.clear()
            location.replace("login.html")
        })
    })
})