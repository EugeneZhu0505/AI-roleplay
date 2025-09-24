import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './styles/homePageStyles.css';
import RoleplayList from './roleplayList';
import RoleplayChat from './roleplayChat';


function HomePage() {
    // 移动端侧边栏控制状态
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [username, setUsername] = useState(localStorage.getItem('username') || '');
    const [image, setImage] = useState(localStorage.getItem('image') || '');

    const navigate = useNavigate();
    
    // 切换侧边栏显示/隐藏
    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const closeSidebar = () => {
        setSidebarOpen(false);
        console.log('关闭侧边栏');
    }
    
    // 退出登录功能
    const handleLogout = () => {
        // 实际项目中这里可以清除用户登录状态
        console.log('用户退出登录');
        toggleSidebar(); // 先关闭侧边栏
        navigate('/'); // 跳转到登录页面
    };

    const handleRoleplayCardClick = (roleplayDetailedInformation) => {
        // navigate('/roleplay', { state: { roleplayId } });
    }
    
    return (
        <div className="home-page">
            {/* 移动端菜单按钮 */}
            <button 
                className={`mobile-menu-button ${sidebarOpen ? 'open' : ''}`}
                onClick={toggleSidebar}
                aria-label="菜单"
            >
                ☰
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
                        ×
                    </button>
                </div>
                <ul className="sidebar-nav">
                    <li>
                        <a href="#dashboard" className="active">
                            <span className="nav-icon">📊</span>
                            <span>仪表盘</span>
                        </a>
                    </li>
                    <li>
                        <a href="#characters">
                            <span className="nav-icon">👤</span>
                            <span>角色管理</span>
                        </a>
                    </li>
                    <li>
                        <a href="#conversations">
                            <span className="nav-icon">💬</span>
                            <span>对话历史</span>
                        </a>
                    </li>
                    <li>
                        <a href="#scenarios">
                            <span className="nav-icon">📜</span>
                            <span>场景库</span>
                        </a>
                    </li>
                    <li>
                        <a href="#settings">
                            <span className="nav-icon">⚙️</span>
                            <span>设置</span>
                        </a>
                    </li>
                    <li>
                        <a href="#logout" onClick={handleLogout}>
                            <span className="nav-icon">🚪</span>
                            <span>退出登录</span>
                        </a>
                    </li>
                </ul>
            </aside>

            {/* 主内容区域 */}
            <main className={`main-content ${sidebarOpen ? 'open' : ''}`}>
                <RoleplayList username={username} image={image} />
            </main>

        </div>
    )
}

export default HomePage;