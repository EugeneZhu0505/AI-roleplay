import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './styles/homePageStyles.css';
import RoleplayList from './roleplayList';
import RoleplayChat from './roleplayChat';
import RoleplayHistoryList from './roleplayHistoryList';
import RoleplaySpeech from './roleyplaySpeech';
import { HistoryAICharacter, SelectedAICharacter } from './utils/historyAICharacter';



const callingCoversationDetail = {
    conversationId: -1,
    userId: -1,
}

function HomePage() {

    // 移动端侧边栏控制状态
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [isChat, setIsChat] = useState(false)
    const [selectedRoleplay, setSelectedRoleplay] = useState({})
    const [isSpeechOpen, setIsSpeechOpen] = useState(false)
    const [callingCoversationDetails, setingCallingCoversationDetails] = useState(callingCoversationDetail);
    const [chatHistoryCharacters, setChatHistoryCharacters] = useState([]);
    const [searchText, setSearchText] = useState("");
    const [filteredCharacters, setFilteredCharacters] = useState([]);

    const navigate = useNavigate();
    
    // 切换侧边栏显示/隐藏
    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const closeSidebar = () => {
        setSidebarOpen(false);
    }


    const handleFinderClick = () => {
        setIsChat(false)
    }

    const handleSpeechClick = (isOpen, callingCoversationDetails) => {
        setIsSpeechOpen(isOpen);
        setingCallingCoversationDetails(callingCoversationDetails)
    }

    const addChatHistoryCharacter = (character) => {
        let updatedCharacters;
        updatedCharacters = [...chatHistoryCharacters];
        updatedCharacters.unshift(character);
        console.log(updatedCharacters);
        setChatHistoryCharacters(updatedCharacters);
    }

    const handleSearchChange = (e) => {
        const searchText = e.target.value;
        setSearchText(searchText);
    }

    useEffect(() => {
        if (searchText) {
            const filteredCharacters = chatHistoryCharacters.filter(
                (character) => character.name.includes(searchText)
            );
            setFilteredCharacters(filteredCharacters);
        } else {
            setFilteredCharacters(chatHistoryCharacters);
        }
    }, [searchText]);


    useEffect(() => {
        setFilteredCharacters(chatHistoryCharacters);
    }, [chatHistoryCharacters]);

    
    
    return (
        <HistoryAICharacter.Provider value={{filteredCharacters, chatHistoryCharacters, addChatHistoryCharacter, setChatHistoryCharacters}}>
        <SelectedAICharacter.Provider value={{selectedRoleplay, setSelectedRoleplay, setIsChat}}>
        <div className='main'>
        <div className={`home-page ${isSpeechOpen ? 'hidden' : ""}`}>

            <button 
                className={`mobile-menu-button ${sidebarOpen ? 'open' : ''}`}
                onClick={toggleSidebar}
                aria-label="菜单"
            >
                <img src={require("../imgs/meau.png")} className='mobile-menu-button-icon'/>
            </button>
            
            {/* 左侧边栏 */}
            <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
                <div className="sidebar-header">
                    <h2>Roleplay.AI</h2>
                    <button
                        className="close-button"
                        onClick={closeSidebar}
                        aria-label="关闭菜单"
                    >
                        <img src={require("../imgs/roll.png")} className='close-button-icon'/>
                    </button>
                </div>

                <div className="sidebar-create-container" onClick={() => navigate('/create')}>
                    <img src={require("../imgs/create.png")} className='sidebar-create-icon'/>
                    <p className='sidebar-create-text'>创建</p>
                </div>

                <div className="sidebar-find-container" onClick={handleFinderClick}>
                    <img src={require("../imgs/find.png")} className='sidebar-find-icon'/>
                    <p className='sidebar-find-text'>发现</p>
                </div>

                <div className="sidebar-search-container">
                    <img src={require("../imgs/search-icon.png")} className='sidebar-search-icon'/>
                    <input type='text' className='sidebar-search-input' placeholder='搜索' onChange={handleSearchChange}/>
                </div>

                <div className="sidebar-history-container">
                    <RoleplayHistoryList />
                </div>

            </aside>

            {/* 主内容区域 */}
            <main className={`main-content ${sidebarOpen ? 'open' : ''}`}>
                {isChat ? (
                    <RoleplayChat roleplayDetailedInformation={selectedRoleplay} handleSpeechClick={handleSpeechClick} />
                ) : (
                    <RoleplayList />
                )}
            </main>

        </div>

        {isSpeechOpen && <RoleplaySpeech handleSpeechClick={handleSpeechClick} callingCoversationDetails={callingCoversationDetails} roleplayDetailedInformation={selectedRoleplay} />}
        </div>
        </SelectedAICharacter.Provider>
        </HistoryAICharacter.Provider>
    )
}

export default HomePage;