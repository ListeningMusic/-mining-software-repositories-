<%--
  Created by IntelliJ IDEA.
  User: 123
  Date: 2019/5/10
  Time: 20:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page  isELIgnored = "false" contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta name="apple-mobile-web-capable" content="yes">
    <title>语音转写</title>
    <link rel="stylesheet" type="text/css" href="css/style.css"/>


</head>
<body>

<div id="container">
    <div id="player">
        <h1>Voice Robot</h1>
        <button id="btn-start-recording" onclick="startRecording();">录音</button>
        <button id="btn-stop-recording" disabled onclick="stopRecording();">停止</button>
        <a id="btn-start-palying" disabled href="http://localhost:8080/auto/hello02">识别</a>
        <div id="inbo">
            <div id="change"></div>
        </div>
        <input type="hidden" id="audiolength">
        <hr>
        <audio id="audioSave" controls autoplay></audio>
        <audio src="music/27-Eversleeping.mp3" controls></audio>
        <textarea id="btn-text-content" class="text-content" ></textarea>
    </div>
</div>
<script type="text/javascript" src="js/jquery-1.11.0.js"></script>
<script type="text/javascript" src="js/HZRecorder.js"></script>
<script src="js/main.js"></script>


</body>
</html>