var HOST_PORT;
var CONTEXT_PATH = "/ftp";

$(document).ready(function () {
    HOST_PORT = getHostPortFromCurrentUrl() === "" ? "127.0.0.1:21011" : getHostPortFromCurrentUrl();
});

function getHostPortFromCurrentUrl() {
    var url = window.location.href;
    if (url.indexOf("localhost") !== -1) {
        return "";
    }
    var reg = new RegExp("^http(s)?://(.*)/.*$");
    if (reg.test(url) === true) {
        var host_port = url.substring(url.indexOf("/") + 2);
        host_port = host_port.substring(0, host_port.indexOf("/"));
        return host_port;
    }
    return "";
}

function executeGet(url, succeeded, failed) {
    $.ajax({
        url: "http://" + HOST_PORT + CONTEXT_PATH + url,
        type: "get",
        contentType: "application/json;charset=utf-8",
        timeout: 30000,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            succeeded(data);
        },
        error: function (jqXHR, textStatus) {
            failed(jqXHR.responseJSON, textStatus);
        }
    });
}

function executePost(url, jsonData, succeeded, failed) {
    $.ajax({
        url: "http://" + HOST_PORT + CONTEXT_PATH + url,
        type: "post",
        contentType: "application/json;charset=utf-8",
        timeout: 30000,
        dataType: "json",
        data: jsonData,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            succeeded(data);
        },
        error: function (jqXHR, textStatus) {
            failed(jqXHR.responseJSON, textStatus);
        }
    });
}