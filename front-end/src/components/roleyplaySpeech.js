import './styles/roleplaySpeech.css'
import { useEffect, useState } from 'react';

const roleplayDetailedInformation = {
    cover: "",
    name: "Roleplay.AI",
    description: "这是一个角色对话的界面",
}

const RoleplaySpeech = ({handleSpeechClick, roleplayDetailedInformation}) => {
    const [isPlaying, setIsPlaying] = useState(false);

    const closeSpeech = () => {
        handleSpeechClick(false)
    }

    // 模拟音频播放状态切换（可以根据实际音频播放状态来控制）
    const togglePlayingState = () => {
        setIsPlaying(!isPlaying);
    }

    return (
        <div className='roleplaySpeech'>
            <div className="roleplaySpeech-container">
                <div className="roleplaySpeech-img-container">
                    <img src={roleplayDetailedInformation.cover} className="roleplaySpeech-img" />
                    <div className="ripple-container">
                        <div className={`ripple ${isPlaying ? 'playing' : ''}`}></div>
                        <div className={`ripple ${isPlaying ? 'playing' : ''}`}></div>
                        <div className={`ripple ${isPlaying ? 'playing' : ''}`}></div>
                        <div className={`ripple ${isPlaying ? 'playing' : ''}`}></div>
                    </div>
                </div>
                <div className="roleplaySpeech-content">
                    <div className="voice-wave">
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                    </div>
                    
                    {/* 临时按钮用于测试播放状态切换 */}
                    <button onClick={togglePlayingState} style={{
                        margin: '20px',
                        padding: '10px 20px',
                        backgroundColor: isPlaying ? '#ff4444' : '#44ff44',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer'
                    }}>
                        {isPlaying ? '停止播放' : '开始播放'}
                    </button>
                </div>

                <div>
                    <button className="roleplaySpeech-close-btn" onClick={closeSpeech}>
                        挂断
                    </button>
                </div>
            </div>
        </div>
    )
}

export default RoleplaySpeech;
