var ROOT_DIR = "/";
var CURRENT_DIR = "/";

$(document).ready(function () {
    getRootDir();

    $("#ftp_cli_btn_back").click(function () {
        displayDir(getBackwardDir(CURRENT_DIR));
    });

    $("#ftp_cli_btn_refresh").click(function () {
        displayDir(CURRENT_DIR);
    });

    $("#ftp_cli_btn_back_to_root").click(function () {
        displayDir(ROOT_DIR);
    });
});

function getRootDir() {
    executeGet(
        "/dir/root",
        function (data) {
            ROOT_DIR = data;
            CURRENT_DIR = data;
            currentDir(CURRENT_DIR);
            displayDir(CURRENT_DIR);
        },
        function (data, code) {
            alert(data);
        }
    );
}

function currentDir(dir) {
    CURRENT_DIR = dir;
    $("#ftp_cli_current_dir").text(dir);
}

function loading() {
    $("#ftp_cli_file_list").attr("style", "display: none");
    $("#loading_status").attr("style", "display: block");
}

function loaded() {
    $("#ftp_cli_file_list").attr("style", "display: block");
    $("#loading_status").attr("style", "display: none");
}

function displayDir(dir) {
    clearListItem();
    currentDir(dir);
    loading();
    executeGet(
        "/list?dir=" + dir,
        function (data) {
            loaded();
            if (data) {
                if (data.length === 0)
                    return;
                for (var i = 0; i < data.length; i++) {
                    addListItem(data[i]);
                }
            }
        },
        function (data, code) {
            alert(data);
        }
    );
}

function clearListItem() {
    $("#ftp_cli_file_list_table_body").empty();
}

function addListItem(data) {
    var type = "None";
    if (data.type === "directory") {
        type = "Dir";
    } else if (data.type === "file") {
        type = "File"
    }
    var path = data.absolutePath;
    var size = data.size;
    var name = data.name;
    if (type === "Dir") {
        name = "<button class='btn-link' onclick='displayDir(\"" + path + "\")'>" + name + "</button>";
    } else if (type === "File") {
        name = "<button class='btn-link'><a href='/ftp/download?file=" + path + "' target='_blank'>" + name + "</a></button>";
    }
    $("#ftp_cli_file_list_table_body").append(
        "<tr>\n" +
        "<td>" + name + "</td>\n" +
        "<td>" + type + "</td>\n" +
        "<td>" + path + "</td>\n" +
        "<td>" + size + "</td>\n" +
        "<td><button class='btn-danger' onclick='removeFile(\"" + path + "\")'>delete</button></td>\n" +
        "</tr>"
    );
}

function removeFile(path) {
    executeGet(
        "/delete?file=" + path,
        function (data) {
            displayDir(CURRENT_DIR);
        },
        function (data, code) {
            alert(data);
        }
    );
}

function getBackwardDir(dir) {
    var backwardDir;
    if (dir.indexOf("/") !== -1) {
        backwardDir = dir.substring(0, dir.lastIndexOf("/"));
        if (backwardDir === "") {
            backwardDir = "/";
        }
        return backwardDir;
    } else if (dir.indexOf("\\") !== -1) { //TODO tobe verified in windows
        backwardDir = dir.substring(0, dir.lastIndexOf("\\"));
        if (backwardDir.endsWith(":")) {
            backwardDir = "C:\\";
        }
        return backwardDir;
    } else {
        return "/";
    }
}