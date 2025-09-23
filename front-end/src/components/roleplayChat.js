import { useState, useRef, useEffect } from 'react';
import './styles/roleplayChat.css';
import { createConversation } from './utils/api';

const RoleplayChat = () => {

    const [messages, setMessages] = useState([
    ])

    const [input, setInput] = useState("");
    const messagesEndRef = useRef(null);
    
    const handleInputChange = (e) => {
        setInput(e)
    };

    const handleSendClick = () => {
        if (input.trim() === "") return;
        setMessages([
            ...messages,
            {
                id: messages.length + 1,
                content: input,
                sender: "user",
            }
        ]);
        setInput("");
    }


    const scrollButtom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "smooth"})
    }

    useEffect(()=>{
        scrollButtom();
    },[messages])

    useEffect(()=> {
        handleCreate();
    },[])


  const handleCreateReponse = (response) =>{
        setMessages([
            ...messages,
            {
                id: messages.length + 1,
                content: response["data"]["opening"],
                sender: "ai",
            }
        ]);
    }  

  const handleCreate = async () => {
    try {
      const result = await createConversation(5, 1, "打招呼");
      console.log('新对话创建成功:', result);
      // 处理成功逻辑（如跳转到对话页、更新列表等   ）
        handleCreateReponse(result)
    } catch (error) {
      alert('创建对话失败: ' + error.message);
    } finally {
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
                        <div className="message-sender">
                        {message.sender === "ai" ? "AI" : "你"}
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
                    <div className='roleplay-image'>
                    </div>
                    <div className='roleplay-build-detail'>

                    </div>
                </div>

                <div className='roleplay-setting-body'>
                    <ur>
                        <div>
                            新聊天
                        </div>
                        <div>
                            语音
                        </div>
                        <div>
                            历史记录
                        </div>
                        <div>
                            角色详细
                        </div>
                        <div>
                            模型选择
                        </div>
                        
                    </ur>
                </div>
            </div>


        
        </div>
    )

}

export default RoleplayChat;