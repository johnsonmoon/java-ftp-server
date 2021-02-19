var ROOT_DIR = "/";

$(document).ready(function () {
    executeGet(
        "/dir/root",
        function (data) {
            ROOT_DIR = data;
            $("#ftp_client_settings_input_form_root_dir").val(ROOT_DIR);
        },
        function (data, code) {
            alert(data.message);
        }
    );

    $("#ftp_client_settings_input_form_submit").click(function () {
        var dirSetting = $("#ftp_client_settings_input_form_root_dir").val();
        executeGet(
            "/dir/root?dir=" + dirSetting,
            function (data) {
                alert(data.toString());
            },
            function (data, code) {
                alert(data.message);
            }
        );
    });
});