import './styles/roleplaySpeech.css';
import { useEffect, useState, useRef, useCallback } from 'react';
import RecordRTC from 'recordrtc'; 
import { sendAudioPost } from './utils/api';

// --- 常量定义 ---
const SILENCE_THRESHOLD = -10; // 静音阈值 (dB)
const SILENCE_DURATION = 2000; // 核心：静音持续时间 (3 秒)

const CALL_STATUS = {
    IDLE: 'IDLE',                       // 闲置/准备状态
    RECORDING: 'RECORDING',             // 正在录制用户语音
    PROCESSING: 'PROCESSING',           // 正在发送WAV文件到服务器
    PLAYING_SERVER_AUDIO: 'PLAYING_SERVER_AUDIO', // 正在播放服务器返回的音频
    ERROR: 'ERROR'                      // 发生错误
};

const RoleplaySpeech = ({ handleSpeechClick, callingCoversationDetails, roleplayDetailedInformation }) => {
    // --- State & Refs ---
    const [callStatus, setCallStatus] = useState(CALL_STATUS.IDLE);
    const [hasSpoken, setHasSpoken] = useState(false);
    const { conversationId, userId } = callingCoversationDetails;

    // UI 状态：用于控制波纹动画
    const isPlayingServerAudio = callStatus === CALL_STATUS.PLAYING_SERVER_AUDIO; 
    const isUserSpeaking = callStatus === CALL_STATUS.RECORDING;

    const recorderRef = useRef(null);
    const streamRef = useRef(null);
    const analyserRef = useRef(null);
    const audioRef = useRef(null);
    const rafIdRef = useRef(null);
    const silenceTimerRef = useRef(null);
    const callStatusRef = useRef(callStatus);
    const hasSpokenRef = useRef(hasSpoken);


    useEffect(() => {
        callStatusRef.current = callStatus; // 每次 currentCallStatus 更新时，同步到 ref
    }, [callStatus]);

    useEffect(() => {
        hasSpokenRef.current = hasSpoken; // 每次 hasSpoken 更新时，同步到 ref
    }, [hasSpoken]);



    // --- III. 服务器通信和播放 ---
    const sendAudioToServer = useCallback(async (audioBlob) => {
        // 1. 发送前确保录音器已完全停止
        if (recorderRef.current) {
            recorderRef.current.pauseRecording();
        }
        
        // 2. 封装 Blob 为 FormData
        const formData = new FormData();
        formData.append('audioFile', audioBlob, 'user_speech.wav');
        formData.append('userId', userId.toString()); 
        formData.append('audioFormat', 'wav'); 
        
        setCallStatus(CALL_STATUS.PROCESSING);
        
        // --- 模拟 API 调用与延迟 ---
        // 实际应用中，您需要将 formData 发送到您的后端 API
        const sendUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${conversationId}/audio-messages?userId=${userId}&inputType=audio`;
        const aiAudioUrl = await sendAudioPost(sendUrl, formData);
        if (!aiAudioUrl) {
            setCallStatus(CALL_STATUS.ERROR);
            return;
        }
        
        // 状态切换到播放
        setCallStatus(CALL_STATUS.PLAYING_SERVER_AUDIO);
        

        // 播放服务器返回的音频
        if (audioRef.current) {
            audioRef.current.src = aiAudioUrl;
            audioRef.current.play().catch(e => {
                setCallStatus(CALL_STATUS.ERROR);
            });
            
            // 重新启动 VAD 监控，以便在播放时检测用户抢占
            rafIdRef.current = requestAnimationFrame(checkActivity); 
        }
    }, []); // sendAudioToServer 是 checkActivity 的依赖，但它本身没有外部依赖

    // --- II. 停止录音并处理 ---
    const stopRecordingAndProcess = useCallback(() => {
        if (!recorderRef.current || recorderRef.current.getState() !== 'recording') return;
        
        // 1. 停止 VAD 监控
        if (rafIdRef.current) cancelAnimationFrame(rafIdRef.current);
        if (silenceTimerRef.current) clearTimeout(silenceTimerRef.current);
        
        // 2. 停止 RecordRTC (获取完整的 Blob)
        recorderRef.current.stopRecording(() => {
            setHasSpoken(false);
            const wavBlob = recorderRef.current.getBlob();
            
            // 3. 关键修复：重置录音器，准备下一次录音
            recorderRef.current = new RecordRTC(streamRef.current, {
                type: 'audio',
                mimeType: 'audio/wav',
                recorderType: RecordRTC.StereoAudioRecorder,
                numberOfAudioChannels: 1 
            });
            
            // 4. 立即启动新的录音器，但保持暂停状态，等待用户说话
            recorderRef.current.startRecording();
            recorderRef.current.pauseRecording();
            
            sendAudioToServer(wavBlob);
        });
    }, [sendAudioToServer]); 


    // --- I. 启动录音 (初始化) ---
    const startRecording = async () => {
        // 清理所有旧的定时器和动画帧
        if (rafIdRef.current) cancelAnimationFrame(rafIdRef.current);
        if (silenceTimerRef.current) clearTimeout(silenceTimerRef.current);
        
        try {
            // 1. 获取麦克风流 (如果已经有了，则跳过)
            let stream = streamRef.current;
            if (!stream) {
                stream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
                streamRef.current = stream;
            }

            // 2. 初始化 AudioContext 和 AnalyserNode (如果没初始化)
            if (!analyserRef.current) {
                const audioContext = new (window.AudioContext || window.webkitAudioContext)();
                const source = audioContext.createMediaStreamSource(stream);
                const analyser = audioContext.createAnalyser();
                analyser.fftSize = 2048; 
                source.connect(analyser);
                // contextRef.current = audioContext; // 建议也保存 contextRef
                analyserRef.current = analyser;
            }
            
            // 3. 初始化 RecordRTC 实例
            if (!recorderRef.current) {
                recorderRef.current = new RecordRTC(stream, {
                    type: 'audio',
                    mimeType: 'audio/wav',
                    recorderType: RecordRTC.StereoAudioRecorder,
                    numberOfAudioChannels: 1 
                });
            }

            // 4. 开始录制和 VAD 监控
            recorderRef.current.startRecording();
            recorderRef.current.pauseRecording(); 
            rafIdRef.current = requestAnimationFrame(checkActivity); 

        } catch(error) {
            console.error('麦克风启动失败:', error);
            setCallStatus(CALL_STATUS.ERROR);
        }
    };
    
    // --- 核心：实时音量分析和静音/活动检测 ---
    const checkActivity = useCallback(() => {
        if (!analyserRef.current) return;

        const callStatus = callStatusRef.current; 
        const analyser = analyserRef.current;
        const dataArray = new Uint8Array(analyser.frequencyBinCount);
        analyser.getByteFrequencyData(dataArray);

        let maxVolume = 0;
        for (let i = 0; i < dataArray.length; i++) {
            if (dataArray[i] > maxVolume) {
                maxVolume = dataArray[i];
            }
        }
        const currentDb = 20 * Math.log10(maxVolume / 255) * 2; 
    

        // --- 逻辑分支 ---
        if (callStatus === CALL_STATUS.RECORDING) {
            // A. 录音状态：检测静音
            if (currentDb < SILENCE_THRESHOLD) {
                if (!silenceTimerRef.current) {
                    silenceTimerRef.current = setTimeout(() => {
                        stopRecordingAndProcess(); 
                    }, SILENCE_DURATION);
                }
                    
            } else {
                // 有语音活动，重置计时器
                if (silenceTimerRef.current) {
                    clearTimeout(silenceTimerRef.current);
                    silenceTimerRef.current = null;
                }
            }
        } 
        else if (callStatus === CALL_STATUS.PLAYING_SERVER_AUDIO) {
            // B. 播放状态：检测用户是否抢占说话
            if (currentDb >= SILENCE_THRESHOLD + 10) { 
                // 核心抢占逻辑
                if (audioRef.current) audioRef.current.pause(); 
                if (recorderRef.current) recorderRef.current.resumeRecording();
                setCallStatus(CALL_STATUS.RECORDING);
                setHasSpoken(true);          
            }
        }
        else if (callStatus === CALL_STATUS.IDLE) {
            // C. 空闲状态：检测用户是否开始说话
            if (currentDb >= SILENCE_THRESHOLD + 10) {
                // 核心优化：直接开始录音，无需等待静音
                if (recorderRef.current) {
                    // 根据录音器当前状态决定调用哪个方法
                    if (recorderRef.current.getState() === 'inactive') {
                        recorderRef.current.startRecording();
                    } else if (recorderRef.current.getState() === 'paused') {
                        recorderRef.current.resumeRecording();
                    }
                }
                setCallStatus(CALL_STATUS.RECORDING);
                setHasSpoken(true);
            }
        }

        rafIdRef.current = requestAnimationFrame(checkActivity);
    }, [stopRecordingAndProcess]);


    // --- IV. 播放结束回调 ---
    const handleAudioEnded = () => {
        // 播放完毕且用户未抢占
        
        // 核心优化：恢复录制，继续捕捉用户语音
        if (recorderRef.current) {
            // 确保录音器处于正确状态
            if (recorderRef.current.getState() === 'inactive') {
                recorderRef.current.startRecording();
            } else if (recorderRef.current.getState() === 'paused') {
                recorderRef.current.resumeRecording();
            }
        }
        
        setCallStatus(CALL_STATUS.RECORDING);
    };

    // --- 生命周期和清理 ---
    const closeSpeech = () => {
        // 清理所有资源
        if (rafIdRef.current) cancelAnimationFrame(rafIdRef.current);
        if (silenceTimerRef.current) clearTimeout(silenceTimerRef.current);
        
        // 销毁 RecordRTC 实例
        if (recorderRef.current) {
             recorderRef.current.destroy(); 
             recorderRef.current = null;
        }
        
        // 停止 MediaStream (关闭麦克风)
        if (streamRef.current) {
             streamRef.current.getTracks().forEach(t => t.stop());
             streamRef.current = null;
        }
        
        // 关闭 AudioContext
        if (analyserRef.current && analyserRef.current.context) {
            analyserRef.current.context.close().catch(() => {});
            analyserRef.current = null;
        }
        
        handleSpeechClick(false); // 挂断电话
    };

    useEffect(() => {
        // 自动启动录音（模拟进入通话即开始）
        startRecording();

        // 组件卸载时，执行清理逻辑
        return () => {
            closeSpeech(); 
        };
    }, []); // 仅在挂载时运行

    // --- 渲染部分 ---
    
    return (
        <div className='roleplaySpeech'>
            <div className="roleplaySpeech-container">
                <div className="roleplaySpeech-img-container">
                    {/* 角色图片 */}
                    <img src={roleplayDetailedInformation.cover} className="roleplaySpeech-img" alt="角色封面" />
                    
                    {/* 播放动画 (服务器在说话) */}
                    <div className="ripple-container">
                        <div className={`ripple ${isPlayingServerAudio ? 'playing' : ''}`}></div>
                        <div className={`ripple ${isPlayingServerAudio ? 'playing' : ''}`}></div>
                        <div className={`ripple ${isPlayingServerAudio ? 'playing' : ''}`}></div>
                        <div className={`ripple ${isPlayingServerAudio ? 'playing' : ''}`}></div>
                    </div>
                </div>
                
                <div className="roleplaySpeech-content">
                    {/* 语音波浪 (用户在说话/录音) */}
                    <div className={`voice-wave ${isUserSpeaking ? 'active' : ''}`}>
                        {/* 这些 bar 通常通过 CSS 动画模拟音量波动 */}
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                    </div>
                    
                </div>

                {/* 隐藏的 </think> 标签用于控制播放 */}
                <audio 
                    ref={audioRef} 
                    onEnded={handleAudioEnded} 
                    style={{ display: 'none' }}
                />

                <div className='roleplaySpeech-close-btn-container'>
                    <button className="roleplaySpeech-close-btn" onClick={closeSpeech}>
                        挂断
                    </button>
                </div>
            </div>
        </div>
    );
}

export default RoleplaySpeech;