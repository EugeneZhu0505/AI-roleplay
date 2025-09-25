import React, { useState, useRef, useEffect } from 'react';
import './styles/roleplayChat.css';
import { createConversation, postRequest } from './utils/api';

const RoleplayChat = ({ roleplayDetailedInformation, handleSpeechClick }) => {

    const [messages, setMessages] = useState([])
    const [input, setInput] = useState("");
    const messagesEndRef = useRef(null);
    

    // 挂载时判断当前AI角色是否存在历史对话
    useEffect(() => {
        if (roleplayDetailedInformation.isHistory) {
            
        }
    }, [roleplayDetailedInformation]);


    const handleInputChange = (value) => {
        setInput(value)
    };

    const handleSendClick = async() => {
        if (input.trim() === "") return;
        handleCreateReponse(input, "user");
        setInput("");
    }


    const scrollButtom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "smooth"})
    }

    useEffect(()=>{
        scrollButtom();
    },[messages])

    // useEffect(()=> {
    //     handleCreate();
    //     postRequest('http://122.205.70.147:8080/api/conversations/1014/activate?userId=5', {})
    // },[])


  const handleCreateReponse = (result, sender) =>{
        setMessages([
            ...messages,
            {
                id: messages.length + 1,
                content: result,
                sender: sender,
            }
        ]);
    }  

//   const handleCreate = async () => {
//     try {
//         const response = await createConversation(5, 1, "打招呼");
//         const data = await response.json();
//         const result = data.data; // 假设API返回格式为{data: ...}

//         // 处理成功逻辑
//         handleCreateReponse(result, "ai");
//     } catch (error) {
//         console.error('创建对话失败:', error);
//         alert('创建对话失败，请稍后重试');
//     }
//   };


    const openSpeech = () =>{
        handleSpeechClick(true)
    }



    return(
        <div className='roleplay-page-container'>
            <div className="roleplay-chat-container">
                <div className="roleplay-chat-header">
                    <img src={roleplayDetailedInformation.cover} className='roleplay-chat-header-image'/>

                </div>

                <div className="roleplay-chat-messages">
                    {messages.map((message) => (
                    <div
                        key={message.id}
                        className={`message ${message.sender === "ai" ? "ai-message" : "user-message"}`}
                    >
                        <div className='message-container'>
                            <img src={require('../imgs/place_charac_image.png')} className='message-image'/>
                            <div className="message-sender">
                                {message.sender === "ai" ? "AI" : "你"}
                            </div>
                        </div>
                        <div className="message-content">{message.content}</div>
                    </div>
                    ))}
                    <div ref={messagesEndRef} />
                </div>

                <div className="roleplay-chat-input">
                    <input
                    className="message-input"
                    type="text"
                    value={input}
                    onChange={(e) => handleInputChange(e.target.value)}
                    placeholder="向 AI角色 发送消息..."
                    />
                    <button className="send-button" onClick={handleSendClick}>
                    发送
                    </button>
                </div>
            </div>

            <div className="roleplay-setting-container">
                <div className='roleplay-setting-header'>
                    <div className='roleplay-image-container'>
                        <img src={roleplayDetailedInformation.cover} className='roleplay-image'/>
                    </div>
                    <div className='roleplay-detail-container'>
                        <div className='AI-role-Name'>{roleplayDetailedInformation?.name || 'AI角色'}</div>
                        <div className='AI-role-builder'>由 @{roleplayDetailedInformation?.builder || '系统'} 创建</div>
                        <div className='AI-role-likeNumber'>{roleplayDetailedInformation?.likeCount || 0} 次互动</div>
                    </div>
                </div>

                <div className='roleplay-setting-body'>
                    <ur className='roleplay-setting-ur-container'>
                        <div className='roleplay-setting-ur-item'>
                            <img src={require('../imgs/chat-new.png')} className='roleplay-setting-image'/>
                            新对话
                        </div>
                        <div className='roleplay-setting-ur-item' onClick={openSpeech}>
                            <img src={require('../imgs/voice.png')} className='roleplay-setting-image'/>
                            语音
                        </div>
                        <div className='roleplay-setting-ur-item'>
                            <img src={require('../imgs/history.png')} className='roleplay-setting-image'/>
                            历史记录
                        </div>
                        <div className='roleplay-setting-ur-item'>
                            <img src={require('../imgs/character.png')} className='roleplay-setting-image'/>
                            角色详细
                        </div>
                        <div className='roleplay-setting-ur-item'>
                            <img src={require('../imgs/modelbim.png')} className='roleplay-setting-image'/>
                            模型
                        </div>
                        
                    </ur>
                </div>
            </div>

        </div>
    )

}

export default RoleplayChat;