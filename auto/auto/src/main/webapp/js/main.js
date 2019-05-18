




var svrUrl ="http://localhost:8080/auto/hello";
//=========================================================================

var recorder = null;
var startButton = document.getElementById('btn-start-recording');
var stopButton = document.getElementById('btn-stop-recording');
var playButton = document.getElementById('btn-start-palying');

//var audio = document.querySelector('audio');
var audio = document.getElementById('audioSave');

function startRecording() {
    //startButton.style.color="red";
    document.getElementById('btn-text-content').value="正在录音...";
    if(recorder != null) {
        recorder.close();
    }
    Recorder.get(function (rec) {
        recorder = rec;
        recorder.start();
    });
    stopButton.disabled = false;
    playButton.disabled = false;
}

function stopRecording() {
    recorder.stop();
    document.getElementById('btn-text-content').value="正在识别...";
    recorder.trans(svrUrl, function(res, errcode){
        if (errcode!=500){
            console.log(res);
           document.getElementById('btn-text-content').value=res;
            //$('#btn-text-content').html("<marquee direction='right'>"+res+"</marquee>");
            document.getElementById('audioSave').src="music/"+res;
            // var filename="music/"+res;
            $('ol').append("<li><a href='#'  class='liShi'>"+res+"</a></li>");
        }

    });
}

function playRecording() {
    recorder.play(audio);
}