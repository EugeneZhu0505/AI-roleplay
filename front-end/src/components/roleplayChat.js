import { useState, useRef, useEffect } from 'react';
import './styles/roleplayChat.css';
import { createConversation, postRequest } from './utils/api';

const RoleplayChat = () => {

    // 保存当前对话的消息列表
    const [messages, setMessages] = useState([])

    // 输入框状态
    const [input, setInput] = useState("");

    const messagesEndRef = useRef(null);
    
    const handleInputChange = (e) => {
        setInput(e)
    };

    const handleSendClick = async() => {
        if (input.trim() === "") return;
        handleCreateReponse(input, "user");
        setInput("");
        
        try {
                // 发送消息并获取响应
                const response = await postRequest('http://122.205.70.147:8080/api/conversations/1014/messages?userId=5', {
                    "message": input,
                    "reponseType": "voice",
                });


                // 检查响应状态码（假设postMessage返回的response包含status属性）
                const result = response.data;
                // 检查result数据结构
                    setMessages([
                        ...messages,
                        {
                            id: messages.length + 1,
                            content: result,
                            sender: "ai",
                        }
                    ]);

            } catch (error) {
                console.error('Error occurred during postMessage:', error);
            }

    }


    const scrollButtom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "smooth"})
    }

    useEffect(()=>{
        scrollButtom();
    },[messages])

    useEffect(()=> {
        handleCreate();
        postRequest('http://122.205.70.147:8080/api/conversations/1014/activate?userId=5', {})
    },[])


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

  const handleCreate = async () => {
    try {
        const reponse = await createConversation(5, 1, "打招呼");

        const result = await reponse.json().data();

        // 处理成功逻辑（如跳转到对话页、更新列表等
        handleCreateReponse(result, "ai");
    } catch (error) {
        alert('创建对话失败: ' + error.message);
    }
  };


    return(
        <div className='roleplay-page-container'>
            <div className="roleplay-chat-container">
                <div className="roleplay-chat-header">
                    <h2>AI角色</h2>
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
                        <img src={require('../imgs/place_charac_image.png')} className='roleplay-image'/>
                    </div>
                    <div className='roleplay-detail-container'>
                        <div className='AI-role-Name'>哈利波特</div>
                        <div className='AI-role-builder'>由 @zy 创建</div>
                        <div className='AI-role-likeNumber'>10.5m 次互动</div>
                    </div>
                </div>

                <div className='roleplay-setting-body'>
                    <ur className='roleplay-setting-ur-container'>
                        <div className='roleplay-setting-ur-item'>
                            <img src={require('../imgs/chat-new.png')} className='roleplay-setting-image'/>
                            新对话
                        </div>
                        <div className='roleplay-setting-ur-item'>
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