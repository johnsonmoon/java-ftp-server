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

    $("#ftp_cli_btn_upload").click(function () {
        var dir = windowsPathSeperatorProcess($("#ftp_cli_current_dir").val());
        uploadFileShow(dir);
    });

    $("#ftp_cli_modal_upload_file_form_file").on("change", function () {
        var name = $("#ftp_cli_modal_upload_file_form_file").val();
        if (name.indexOf("/") !== -1) {
            name = name.substring(name.lastIndexOf("/") + 1, name.length);
        } else if (name.indexOf("\\") !== -1) {
            name = name.substring(name.lastIndexOf("\\") + 1, name.length);
        }
        $("#ftp_cli_modal_upload_file_form_file_name").val(name);
    });

    $("#ftp_cli_current_dir_btn_goto").click(function () {
        var dir = windowsPathSeperatorProcess($("#ftp_cli_current_dir").val());
        displayDir(dir);
    });

    $("#ftp_cli_current_dir").keydown(function (event) {
        if (event.which === 13) {
            var dir = windowsPathSeperatorProcess($("#ftp_cli_current_dir").val());
            displayDir(dir);
        }
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
            alert(data.message);
        }
    );
}

function currentDir(dir) {
    CURRENT_DIR = dir;
    $("#ftp_cli_current_dir").val(dir);
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
            alert(data.message);
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
    var path = windowsPathSeperatorProcess(data.absolutePath);
    var size = data.size;
    var name = data.name;
    if (type === "Dir") {
        name = "<button class='btn-link' onclick='displayDir(\"" + path + "\")'>" + name + "</button>";
    } else if (type === "File") {
        name = "<button class='btn-link'><a href='/ftp/download?file=" + path + "' target='_blank'>" + name + "</a></button>";
    }
    var operation = "<td></td>\n";
    if (type === "File") {
        operation =
            "<td>" +
            "<button class='btn-danger' onclick='deleteFileShow(\"" + path + "\")'>delete</button> " +
            "<button class='btn-success' onclick='renameFileShow(\"" + path + "\")'>rename</button> " +
            "<button class='btn-info' onclick='moveFileShow(\"" + path + "\")'>move</button>" +
            "</td>\n";
    }
    $("#ftp_cli_file_list_table_body").append(
        "<tr>\n" +
        "<td>" + name + "</td>\n" +
        "<td>" + type + "</td>\n" +
        "<td>" + path + "</td>\n" +
        "<td>" + size + "</td>\n" +
        operation +
        "</tr>"
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

function deleteFileShow(fileDir) {
    $("#ftp_cli_modal_delete_file").attr("class", "modal fade in");
    $("#ftp_cli_modal_delete_file").attr("style", "display: block");
    $("#ftp_cli_modal_delete_file_input_form_input_file_dir").val(fileDir);
}

function deleteFileHide() {
    $("#ftp_cli_modal_delete_file").attr("class", "modal fade");
    $("#ftp_cli_modal_delete_file").attr("style", "display: none");
}

function deleteFile() {
    var fileDir = $("#ftp_cli_modal_delete_file_input_form_input_file_dir").val();
    executeGet(
        "/delete?file=" + fileDir,
        function (data) {
            deleteFileHide();
            displayDir(CURRENT_DIR);
        },
        function (data, code) {
            alert(data.message);
            deleteFileHide();
            displayDir(CURRENT_DIR);
        }
    );
}

function renameFileShow(fileDir) {
    $("#ftp_cli_modal_rename_file").attr("class", "modal fade in");
    $("#ftp_cli_modal_rename_file").attr("style", "display: block");
    $("#ftp_cli_modal_rename_file_input_form_input_file_dir").val(fileDir);
    $("#ftp_cli_modal_rename_file_input_form_input_target_name").val(fileDir);
}

function renameFileHide() {
    $("#ftp_cli_modal_rename_file").attr("class", "modal fade");
    $("#ftp_cli_modal_rename_file").attr("style", "display: none");
}

function renameFile() {
    var fileDir = $("#ftp_cli_modal_rename_file_input_form_input_file_dir").val();
    var targetName = $("#ftp_cli_modal_rename_file_input_form_input_target_name").val();
    if (fileDir === "" || targetName === "") {
        alert("file or target name must not be null!");
    }
    executeGet(
        "/rename?file=" + fileDir + "&targetFile=" + targetName,
        function (data) {
            renameFileHide();
            displayDir(CURRENT_DIR);
        },
        function (data, code) {
            alert(data.message);
            renameFileHide();
            displayDir(CURRENT_DIR);
        }
    );
}

function moveFileShow(fileDir) {
    $("#ftp_cli_modal_move_file").attr("class", "modal fade in");
    $("#ftp_cli_modal_move_file").attr("style", "display: block");
    $("#ftp_cli_modal_move_file_input_form_input_file_dir").val(fileDir);
    $("#ftp_cli_modal_move_file_input_form_input_target_dir").val(CURRENT_DIR);
}

function moveFileHide() {
    $("#ftp_cli_modal_move_file").attr("class", "modal fade");
    $("#ftp_cli_modal_move_file").attr("style", "display: none");
}

function moveFile() {
    var fileDir = $("#ftp_cli_modal_move_file_input_form_input_file_dir").val();
    var targetDir = $("#ftp_cli_modal_move_file_input_form_input_target_dir").val();
    if (fileDir === "" || targetDir === "") {
        alert("file or target dir must not be null!");
    }
    executeGet(
        "/move?file=" + fileDir + "&targetDir=" + targetDir,
        function (data) {
            moveFileHide();
            displayDir(CURRENT_DIR);
        },
        function (data, code) {
            alert(data.message);
            moveFileHide();
            displayDir(CURRENT_DIR);
        }
    );
}

function uploadFileShow(fileDir) {
    $("#ftp_cli_modal_upload_file").attr("class", "modal fade in");
    $("#ftp_cli_modal_upload_file").attr("style", "display: block");
    $("#ftp_cli_modal_upload_file_form_target_dir").val(fileDir);
}

function uploadFileHide() {
    $("#ftp_cli_modal_upload_file").attr("class", "modal fade");
    $("#ftp_cli_modal_upload_file").attr("style", "display: none");
}

function uploadFile() {
    var fileName = $("#ftp_cli_modal_upload_file_form_file_name").val();
    var targetDir = $("#ftp_cli_modal_upload_file_form_target_dir").val();
    if (fileName === "" || targetDir === "") {
        alert("File name or target dir must not be null.");
    }
    uploadForm(
        "#ftp_cli_modal_upload_file_form",
        "/upload",
        function () {
        },
        function (data) {
            uploadFileHide();
            displayDir(CURRENT_DIR);
        },
        function (data, status) {
            alert(data.message);
            uploadFileHide();
            displayDir(CURRENT_DIR);
        }
    );
}

function windowsPathSeperatorProcess(path) {
    if (path.indexOf("\\") !== -1) {
        path = path.replaceAll("\\", "\\\\");
    }
    return path;
}