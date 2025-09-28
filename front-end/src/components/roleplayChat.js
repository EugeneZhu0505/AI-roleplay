import { useState, useRef, useEffect , useCallback, useContext} from 'react';
import './styles/roleplayChat.css';
import { createConversationPost, activeConversationPost, sendMessagePost, getApiNotBody } from './utils/api';
import {HistoryAICharacter} from './utils/historyAICharacter';
import Popup from "./popup";
import PullUpMenu from "./pullUpMenu";



const RoleplayChat = ({ roleplayDetailedInformation, handleSpeechClick}) => {
    const [messagesHistory, setMessagesHistory] = useState([])
    const [conversationId, setConversationId] = useState(-1);
    const [input, setInput] = useState("");
    const [characterId, setCharacterId] = useState(-1);
    const userId = JSON.parse(localStorage.getItem("login-success-user")).userId;
    const userName = JSON.parse(localStorage.getItem("login-success-user")).username;
    const characterName = roleplayDetailedInformation.name;
    const messagesEndRef = useRef(null);
    const streamControllerRef = useRef(null);
    const conversationIdRef = useRef(null);
    const characterIdRef = useRef(null);
    const {addChatHistoryCharacter} = useContext(HistoryAICharacter);
    const [isPopupOpen, setIsPopupOpen] = useState(false);
    const [popupContent, setPopupContent] = useState(null);
    const [selectedSkill, setSelectedSkill] = useState("角色技能");

    // 挂载和点击不同角色时
    useEffect(() => {
        setMessagesHistory([]);

        setCharacterId(roleplayDetailedInformation.characterId);
        characterIdRef.current = roleplayDetailedInformation.characterId;

        if(roleplayDetailedInformation.conversationId){
            setConversationId(roleplayDetailedInformation.conversationId);
            conversationIdRef.current = roleplayDetailedInformation.conversationId;

            const messageHistoryUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${conversationIdRef.current}/messages?userId=${userId}`;
            getApiNotBody(messageHistoryUrl)
            .then((response) => {
                // 处理历史消息
                response.data.forEach((message) => {
                    handleMessageHistory(message.textContent, message.senderType === "character" ? "ai" : "user");
                })
            })
            .catch((_) => {
                alert('获取对话历史失败，请稍后重试');
            })
        } else {
            // 获取特定角色的对话列表, 判断是否有历史对话
            const conversationHistoryUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/character/${characterIdRef.current}?userId=${userId}`;
            getApiNotBody(conversationHistoryUrl)
            .then((response) => {
                if (response.data.length > 0){
                    setConversationId(response.data[0].id);
                    conversationIdRef.current = response.data[0].id;

                    if(conversationIdRef.current !== -1){
                        // 激活对话
                        const activeUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${conversationIdRef.current}/activate?userId=${userId}`;
                        activeConversationPost(activeUrl).catch((_) => {alert('激活对话失败，请稍后重试'); })

                        const messageHistoryUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${conversationIdRef.current}/messages?userId=${userId}`;
                        getApiNotBody(messageHistoryUrl).then((response) => {
                            // 处理历史消息
                            response.data.forEach((message) => {
                                handleMessageHistory(message.textContent, message.senderType === "character" ? "ai" : "user");
                            })
                        }).catch((_) => {
                            alert('获取对话历史失败，请稍后重试');
                        })
                    }
                } else {
                    setConversationId(-1);
                    conversationIdRef.current = -1;

                    const createConversationUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations?userId=${userId}&characterId=${roleplayDetailedInformation.characterId}&title=与${roleplayDetailedInformation.name}的对话`;
                    createConversationPost(createConversationUrl)
                    .then((response) => {
                        setConversationId(response.data.conversation.id);
                        conversationIdRef.current = response.data.conversation.id;
                        handleMessageHistory(response.data.opening, "ai");

                        // 加入历史记录
                        addChatHistoryCharacter({
                            conversationId: response.data.conversation.id,
                            characterId: response.data.conversation.characterId,
                            cover: roleplayDetailedInformation.cover,
                            name: roleplayDetailedInformation.name,
                            title: response.data.conversation.title,
                        })

                        
                    })
                    .catch((_) => {
                        alert('创建对话失败，请稍后重试');
                    })  
                }
            })
            .catch((_) => {
                alert('获取对话历史失败，请稍后重试');
            })
        }


    }, [roleplayDetailedInformation]);


    const handleInputChange = (value) => {
        setInput(value)
    };

    const scrollButtom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "auto"})
    };

    useEffect(()=>{
        scrollButtom();
    },[messagesHistory])


  const handleMessageHistory = (result, sender) =>{
        setMessagesHistory((prevMessages) => [ 
            ...prevMessages,
            {
                id: prevMessages.length > 0 ? prevMessages[prevMessages.length - 1].id + 1 : 1,
                content: result,
                sender: sender,
            }
        ]);
    }  


    const openSpeech = () =>{
        handleSpeechClick(true, {
            conversationId: conversationId,
            userId: userId,
        })
    }

    const handleSendClick = async() => {
        if (input.trim() === "") return;
        handleMessageHistory(input, "user");

        let sendUrl = ""

        if(selectedSkill === "角色技能"){
            sendUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${conversationId}/text-messages?userId=${userId}&message=${input}`;
        } else {
            sendUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${conversationId}/text-messages?userId=${userId}&message=${input}&skill=${selectedSkill}`;
        }
        console.log(sendUrl)

        setInput("");
        await handleSendMessageResponse(sendUrl, {});
    }

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSendClick();
        }
    };

    const handleSendMessageResponse = useCallback(async(url, message) => {
        try {
            let responseId
            setMessagesHistory((prev) => {
                responseId = prev.length > 0 ? prev[prev.length - 1].id + 1 : 1;
                return [
                ...prev,
                {
                    id: responseId,
                    content: "",
                    sender: "ai",
                }
            ]});

            // 获取流读取器
            const reader = await sendMessagePost(url, message,);

            // 创建流控制器
            const abortController = new AbortController();
            streamControllerRef.current = abortController;

            const decoder = new TextDecoder();
            let buffer = ""

            while(true){
                // 每次读取一个数据块
                const {value, done} = await reader.read()
                if(done || abortController.signal.aborted){
                    break;
                }

                const chunk = decoder.decode(value, {stream: true});
                buffer += chunk;

                const lines = buffer.split("\n");
                buffer = lines.pop();

                for(const line of lines){
                    let newData = ""
                    if(line.startsWith("data:")){
                        newData = line.slice(11).trim();
                        if(newData === "[DONE]") {
                            reader.cancel();
                            streamControllerRef.current = null;
                            return;
                        }
                    }
                    
                    if(newData !== ""){
                        setMessagesHistory(
                            (prev) => {
                                const newMessages = [...prev];
                                const index = newMessages.findIndex((msg) => msg.id === responseId);

                                if (index !== -1) {
                                    let contentToAdd = newData;
                                    try {
                                        const parsedData = JSON.parse(newData);
                                        contentToAdd = parsedData.text || parsedData.content || newData; 
                                    } catch (error) {
                                    // 非 JSON 数据，直接使用 newData
                                        contentToAdd = newData;
                                    }
                                
                                    // 3. 更新消息内容
                                    newMessages[index] = {
                                        ...newMessages[index], 
                                        content: newMessages[index].content + contentToAdd
                                    };
                                }
                                return newMessages
                            })
                     }
                }
            }
        } catch (error) {
            alert('发送消息失败，请稍后重试');
        } finally{
            if (streamControllerRef.current && typeof streamControllerRef.current.cancel === "function") {
                streamControllerRef.current.cancel();
                streamControllerRef.current = null;
            }
        }
    }, [characterId, userId]);


    const handleNewConversation = async() => {
        setConversationId(-1);
        conversationIdRef.current = -1;
        setMessagesHistory([]);

        const createConversationUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations?userId=${userId}&characterId=${roleplayDetailedInformation.characterId}&title=与${roleplayDetailedInformation.name}的对话`;
        try{
            const response = await createConversationPost(createConversationUrl)
            setConversationId(response.data.conversation.id);
            conversationIdRef.current = response.data.conversation.id;
            handleMessageHistory(response.data.opening, "ai");

            // 加入历史记录
            addChatHistoryCharacter({
                conversationId: response.data.conversation.id,
                characterId: response.data.conversation.characterId,
                cover: roleplayDetailedInformation.cover,
                name: roleplayDetailedInformation.name,
                title: response.data.conversation.title,
            })
        } 
        catch (error) {
            alert('创建新对话失败，请稍后重试');
        }
       
    
    }


    const handleCharacterDetail = async() => {
        const url = `${process.env.REACT_APP_API_BASE_URL}/api/characters/${characterIdRef.current}`;

        try{
            const response = await getApiNotBody(url);
            setPopupContent(response.data);
            setIsPopupOpen(true);
            
        }catch (error) {
            alert('获取角色详情失败，请稍后重试');
        }

    }

    const closePopup = () => {
        setIsPopupOpen(false);
    };

    const handleSkillSelect = (skill) => {
        setSelectedSkill(skill);
    }


    return(
        <div className='roleplay-page-container'>
            <div className="roleplay-chat-container">
                <div className="roleplay-chat-header">
                    <img src={roleplayDetailedInformation.cover} className='roleplay-chat-header-image'/>

                </div>

                <div className="roleplay-chat-messages">
                    {messagesHistory.map((message) => (
                    <div
                        key={message.id}
                        className={`message ${message.sender === "ai" ? "ai-message" : "user-message"}`}
                    >
                        <div className='message-container'>
                            <img src={message.sender === "ai" ? roleplayDetailedInformation.cover : JSON.parse(localStorage.getItem("login-success-user")).avatarUrl} className='message-image'/>
                            <div className="message-sender">
                                {message.sender === "ai" ? `${characterName}` : `${userName}`}   
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
                    <button className="send-button" onClick={handleSendClick} onKeyDown={handleKeyDown}>
                    发送
                    </button>
                    <div class="roleplay-skill-pull-up">           
                        <PullUpMenu characterId={roleplayDetailedInformation.characterId} onSelect={handleSkillSelect} />
                    </div>
                </div>
            </div>

            <div className="roleplay-setting-container">
                <div className='roleplay-setting-header'>
                    <div className='roleplay-image-container'>
                        <img src={roleplayDetailedInformation.cover} className='roleplay-image'/>
                    </div>
                    <div className='roleplay-detail-container'>
                        <div className='AI-role-Name'>{roleplayDetailedInformation?.name || 'AI角色'}</div>
                        <div className='AI-role-builder'>{roleplayDetailedInformation?.title}</div>
                    </div>
                </div>

                <div className='roleplay-setting-body'>
                    <div className='roleplay-setting-ur-container'>
                        <div className='roleplay-setting-ur-item' onClick={handleNewConversation}>
                            <img src={require('../imgs/chat-new.png')} className='roleplay-setting-image'/>
                            新对话
                        </div>
                        <div className='roleplay-setting-ur-item' onClick={openSpeech}>
                            <img src={require('../imgs/voice.png')} className='roleplay-setting-image'/>
                            语音
                        </div>
                        <div className='roleplay-setting-ur-item' onClick={handleCharacterDetail}>
                            <img src={require('../imgs/character.png')} className='roleplay-setting-image'/>
                            角色详细
                        </div>
                        <Popup isOpen={isPopupOpen} onClose={closePopup} content={popupContent}/>
                        <div className='roleplay-setting-ur-item'>
                            <img src={require('../imgs/modelbim.png')} className='roleplay-setting-image'/>
                            模型
                        </div>
                        
                    </div>
                </div>
            </div>

        </div>
    )

}

export default RoleplayChat;