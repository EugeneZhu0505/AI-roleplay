import './styles/roleplaySpeech.css'
import { useEffect } from 'react';

const roleplayDetailedInformation = {
    cover: "",
    name: "Roleplay.AI",
    description: "这是一个角色对话的界面",
}

const RoleplaySpeech = ({handleSpeechClick, roleplayDetailedInformation}) => {

    const closeSpeech = () => {
        handleSpeechClick(false)
    }

    return (
        <div className='roleplaySpeech'>
            <div className="roleplaySpeech-container">
                <img src={roleplayDetailedInformation.cover} className="roleplaySpeech-img" />
                <div className="roleplaySpeech-content">
                    <div className="voice-wave">
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                        <span className="bar" />
                    </div>    
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
