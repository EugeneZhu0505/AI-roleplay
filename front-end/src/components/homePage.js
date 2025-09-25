import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './styles/homePageStyles.css';
import RoleplayList from './roleplayList';
import RoleplayChat from './roleplayChat';
import RoleplayHistoryList from './roleplayHistoryList';
import RoleplaySpeech from './roleyplaySpeech';

const roleplayDetailedInformation = {
    key: '1',
    cover: 'https://img.qiniu.ai/roleplay/1.png',
    name: '角色名',
    builder: '构建者',
    desc: '角色描述',
    likeCount: 0,
}

function HomePage() {

    // 移动端侧边栏控制状态
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [username, setUsername] = useState(localStorage.getItem('username') || '');
    const [image, setImage] = useState(localStorage.getItem('image') || '');
    const [isChat, setIsChat] = useState(false)
    const [selectedRoleplay, setSelectedRoleplay] = useState(null)
    const [isSpeechOpen, setIsSpeechOpen] = useState(false)

    const navigate = useNavigate();
    
    // 切换侧边栏显示/隐藏
    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const closeSidebar = () => {
        setSidebarOpen(false);
        console.log('关闭侧边栏');
    }
    

    const handleRoleplayCardClick = (roleplayDetailedInformation) => {
        console.log('点击了角色', roleplayDetailedInformation);
        setSelectedRoleplay(roleplayDetailedInformation)
        setIsChat(true)
    }

    const handleFinderClikc = () => {
        setSelectedRoleplay(null)
        setIsChat(false)
    }

    const handleSpeechClick = (isOpen) => {
        setIsSpeechOpen(isOpen);
    }
    
    
    return (
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

                <div className="sidebar-create-container">
                    <img src={require("../imgs/create.png")} className='sidebar-create-icon'/>
                    <p className='sidebar-create-text'>创建</p>
                </div>

                <div className="sidebar-find-container" onClick={handleFinderClikc}>
                    <img src={require("../imgs/find.png")} className='sidebar-find-icon'/>
                    <p className='sidebar-find-text'>发现</p>
                </div>

                <div className="sidebar-search-container">
                    <img src={require("../imgs/search-icon.png")} className='sidebar-search-icon'/>
                    <input type='text' className='sidebar-search-input' placeholder='搜索'/>
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
                    <RoleplayList handleRoleplayCardClick={handleRoleplayCardClick} username={username} image={image} />
                )}
            </main>

        </div>

        {isSpeechOpen && <RoleplaySpeech handleSpeechClick={handleSpeechClick} roleplayDetailedInformation={selectedRoleplay} />}
        </div>
    )
}

export default HomePage;